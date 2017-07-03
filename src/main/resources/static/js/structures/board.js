var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.GameBoard = function (gameMaster, gameConfig, eventManager) {
        this.master = gameMaster;

        this.config = gameConfig;

        this.eventManager = eventManager;
        this.events = this.config.getOptionGroup("events").byBoard;

        this.unitFactory = undefined;
        this.cellFactory = undefined;
        this.prepareFactories();

        this.lastMap = undefined;
        this.loadMap("map01dm01");

        this.READONLY = false;

        return this;
    };

    xSSSR.GameBoard.prototype = {
        prepareFactories: function () {
            var mapconf = this.config.getOptionGroup("map");
            var typeconf = mapconf.unitType;
            var rankconf = mapconf.unitRank;
            var sideconf = mapconf.side;

            this.unitFactory = new xSSSR.UnitFactory(this.config);
            this.unitFactory.design(typeconf.PRINCE, {"type": rankconf.NOBLE, "side": sideconf.DEFENDER});
            this.unitFactory.design(typeconf.WARDEN, {"type": rankconf.WARRIOR, "side": sideconf.DEFENDER});
            this.unitFactory.design(typeconf.VIKING, {"type": rankconf.WARRIOR, "side": sideconf.ATTACKER});

            var boardconf = this.config.getOption("board", "floor");

            this.cellFactory = new xSSSR.CellFactory(this.config);
            this.cellFactory.design("border", {"floor": boardconf.DISABLED});
            this.cellFactory.design("normal", {"floor": boardconf.NORMAL});
            this.cellFactory.design("throne", {"floor": boardconf.THRONE});
            this.cellFactory.design("exit", {"floor": boardconf.EXIT});
        },

        clearMemory: function () {
            this.READONLY = false;
            this.boardSize = 0;	// Option: Main parameter
            this.fieldSize = 0;	// Shortcut: calculated from Boardsize
            this.upperLeftCorner = {x: 0, y: 0};	// Shortcut
            this.currentTurn = 0;	// Property
            this.activeSide = 0;	// Property
            this.mySide = 0;
            this.nobleMaxMovement = 0;	// Option:
            this.cells = [];	// Property
            this.princeLocationId = 0;	// Fast look-up: for noble-defending unit
            this.throneLocationId = 0;	// Fast look-up: for throne room floor cell
        },

        restart: function () {
            this.init(this.lastMap);
        },

        loadMap: function (mapName) {
            var mapJson = `{
	"halfSize"			: 4,
	"currentTurn"		: 0,
	"activeSide" 		: 0,
	"mySide":0,
	"nobleMaxMovement"	: 3,
	
	"exits" 			: [
		{"x" : 1, "y" : 1},
		{"x" : 9, "y" : 1},
		{"x" : 1, "y" : 9},
		{"x" : 9, "y" : 9}
	],
	
	"throneRoom"		: {"x" : 5, "y" : 5},
	
	"startingPosition": {
		"units" : [
			{ "type" : "viking", "position" : {"x" : 4, "y" : 1} },
			{ "type" : "viking", "position" : {"x" : 5, "y" : 1} },
			{ "type" : "viking", "position" : {"x" : 6, "y" : 1} },
			{ "type" : "viking", "position" : {"x" : 4, "y" : 9} },
			{ "type" : "viking", "position" : {"x" : 5, "y" : 9} },
			{ "type" : "viking", "position" : {"x" : 6, "y" : 9} },

			{ "type" : "viking", "position" : {"x" : 1, "y" : 4} },
			{ "type" : "viking", "position" : {"x" : 1, "y" : 5} },
			{ "type" : "viking", "position" : {"x" : 1, "y" : 6} },
			{ "type" : "viking", "position" : {"x" : 9, "y" : 4} },
			{ "type" : "viking", "position" : {"x" : 9, "y" : 5} },
			{ "type" : "viking", "position" : {"x" : 9, "y" : 6} },		
			
			{ "type" : "viking", "position" : {"x" : 5, "y" : 2} },
			{ "type" : "viking", "position" : {"x" : 2, "y" : 5} },
			{ "type" : "viking", "position" : {"x" : 8, "y" : 5} },
			{ "type" : "viking", "position" : {"x" : 5, "y" : 8} },	
		
			{ "type" : "warden", "position" : {"x" : 4, "y" : 5} },
			{ "type" : "warden", "position" : {"x" : 6, "y" : 5} },
			{ "type" : "warden", "position" : {"x" : 5, "y" : 4} },
			{ "type" : "warden", "position" : {"x" : 5, "y" : 6} },
			{ "type" : "warden", "position" : {"x" : 3, "y" : 5} },
			{ "type" : "warden", "position" : {"x" : 7, "y" : 5} },
			{ "type" : "warden", "position" : {"x" : 5, "y" : 3} },
			{ "type" : "warden", "position" : {"x" : 5, "y" : 7} }
		],
		"primary" : { "x" : 5, "y" : 5 }					
	}	
}`;
            var map = JSON.parse(mapJson);
            var template = this.normalizeTemplate(map);
            this.init(template);
            this.eventManager.fire(this.events.mapLoaded, [mapName]);
        },

        init: function (template) {
            this.clearMemory();

            this.lastMap = template;

            this.boardSize = template.boardSize;
            this.fieldSize = template.fieldSize;
            this.upperLeftCorner = template.upperLeftCorner;
            this.currentTurn = template.currentTurn;
            this.activeSide = template.activeSide;
            this.mySide = template.mySide;
            this.nobleMaxMovement = template.nobleMaxMovement;

            // Init all cells on the field as disabled
            for (var i = 0; i < this.fieldSize * this.fieldSize; ++i) {
                this.cells[i] = this.cellFactory.spawn("border");
            }

            var temp;
            // Mark inner cells (board) as passable
            var passableCell = this.cellFactory.spawn("normal");
            for (var j = 1; j <= this.boardSize; ++j) {
                for (var i = 1; i <= this.boardSize; ++i) {
                    temp = this.xyToCellId(i, j);
                    this.cells[temp].floor = passableCell.floor;
                }
            }

            // Mark all possible exits on the board
            var exitCell = this.cellFactory.spawn("exit");
            for (var i = 0; i < template.exits.length; ++i) {
                temp = this.pointToCellId(template.exits[i]);
                this.cells[temp].floor = exitCell.floor;
            }

            // Place throne room on the board
            this.throneLocationId = this.pointToCellId(template.throneRoom);

            this.cells[this.throneLocationId] = this.cellFactory.spawn("throne");

            // Spawn ordinary units
            function proceedUnitPack(sourceArray) {
                var unit;
                var pos;
                for (var i = 0; i < sourceArray.length; ++i) {
                    unit = sourceArray[i];
                    pos = this.pointToCellId(unit.position);
                    this.cells[pos].unit = this.unitFactory.spawn(unit.type);
                }
            }

            proceedUnitPack.call(this, template.startingPosition.units);

            // Spawn Prince(Knyaz) - the main unit in game
            this.princeLocationId = this.pointToCellId(template.startingPosition.primary);

            this.cells[this.princeLocationId].unit = this.unitFactory.spawn("prince");
        },

        passTheTurn: function () {
            ++this.currentTurn;
            this.activeSide = this.nextSide(this.activeSide);
        },

        nextSide: function (side) {
            return (side + 1) % 2;
        },

        normalizeTemplate: function (template) {
            var result = template;
            var realSize = parseInt(template.halfSize) * 2 + 1;

            result.boardSize = realSize;
            result.fieldSize = realSize + 2;
            result.upperLeftCorner = {x: 1, y: 1};
            result.currentTurn = parseInt(template.currentTurn);
            result.nobleMaxMovement = parseInt(template.nobleMaxMovement);

            var sideconf = this.config.getOption("map", "side");
            if ((!(template.activeSide == sideconf.ATTACKER)) || (!(template.activeSide == sideconf.DEFENDER)))
                result.activeSide = sideconf.ATTACKER;

            return result;
        },

        move: function (origin, destination) {
            if (this.READONLY) {
                return false;
            }

            if (!this.validMove(origin, destination)) {
                return false;
            }

            this.cells[destination].unit = this.cells[origin].unit;
            this.cells[origin].unit = false;
            this.eventManager.fire(this.events.unitMoved, [origin, destination]);

            this.trackThePrince(origin, destination);
            this.captureNeighbors(destination);

            var state = this.getGameState();
            if (this.gameInProcess(state)) {
                this.passTheTurn();
            }
            else {
                this.READONLY = true;
                this.eventManager.fire(this.events.gameStateChangedTo, [state]);
            }
        },

        trackThePrince: function (fromCellId, toCellId) {
            if (this.princeLocationId === fromCellId)
                this.princeLocationId = toCellId;
        },

        captureNeighbors: function (cellId) {
            var neighbors = this.getAdjacentCellIds(cellId);
            for (var i = 0; i < neighbors.length; ++i) {
                if (this._canCaptureFigureOn(neighbors[i])) {
                    this._doCaptureFigureOn(neighbors[i])
                }
            }
        },

        _canCaptureFigureOn: function (targetCellId) {
            var surroundedInField = this.targetSurrounded(targetCellId);
            var surroundedNearThrone = this.targetSurroundedUsingThroneRule(targetCellId);
            var surrounded = surroundedInField || surroundedNearThrone;

            if (this.princeLocationId == targetCellId) {
                if ((this.throneLocationId == targetCellId) || (this.twoCellsAdjacent(this.princeLocationId, this.throneLocationId))) {	// Prince cannot be captured near or on Throne using normal rules
                    return false;
                } else {
                    return surroundedInField;
                }
            } else {
                return surrounded;
            }
        },

        _doCaptureFigureOn: function (targetCellId) {
            this.cells[targetCellId].unit = false;
            this.eventManager.fire(this.events.unitCapturedAt, [targetCellId]);
        },

        getGameState: function () {
            var directions = this.config.getOption("board", "adjacency");
            var stateconf = this.config.getOption("board", "gamestate");
            if (this.cellFactory.isGoal(this.cells[this.princeLocationId])) {
                return stateconf.PRINCEESCAPED;
            }
            if (!this.isVikingUnitsExists()) {
                return stateconf.VIGINGSDEFEATED;
            }
            var evilBastardsAround = this.countAdjacentEnemyUnits(this.princeLocationId); //0..4
            if (this.princeLocationId === this.throneLocationId) {
                if (evilBastardsAround === directions.length)			// GAME INVARIANT: all cells around filled with enemies
                {
                    return stateconf.PRINCESTUCKINTHRONEROOM;
                }
            } else if (this.twoCellsAdjacent(this.princeLocationId, this.throneLocationId)) {
                if (evilBastardsAround === (directions.length - 1))	// GAME INVARIANT: all cells around except empty Throne
                {
                    return stateconf.PRINCEBLOCKED;
                }
            } else if (this.cells[this.princeLocationId].unit === false)
            //if(this.targetSurrounded(this.princeLocationId))
            {
                return stateconf.PRINCESURROUNDED;
            }

            return stateconf.ONLINE;
        },

        targetSurrounded: function (cellId) {
            var unit = this.cells[cellId].unit;
            if (unit === false) {
                return false;
            }

            var neighbors = this.getAdjacentCellsTo(cellId);

            // Surrounded - kept between two sources of danger
            function isSurrounded(target, neighbor1, neighbor2) {
                // Both enemies must contribute danger
                function isDangerous(target, cell) {
                    return !!(( ( cell.unit ) && ( cell.unit.enemyTo(target) ) )
                    || ( this.cellFactory.isGoal(cell) ));


                }

                // Assumption made: no unit can stay between two Exit cells
                return isDangerous.call(this, target, neighbor1) && isDangerous.call(this, target, neighbor2);
            }

            // Two variants - vertical and horizontal
            var horizontalMenace = isSurrounded.call(this, unit, neighbors[0], neighbors[1]);
            var verticalMenace = isSurrounded.call(this, unit, neighbors[2], neighbors[3]);

            return horizontalMenace || verticalMenace;
        },

        getAdjacentCellIds: function (cellId) {
            var directions = this.config.getOption("board", "adjacency");
            var position = this.cellIdToPoint(cellId);

            var neighborsIds = [];
            for (var i = 0; i < directions.length; ++i) {
                neighborsIds[i] = this.pointToCellId(this.shiftPoint(position, directions[i]));
            }
            return neighborsIds;
        },

        getAdjacentCellsTo: function (cellId) {
            var directions = this.config.getOption("board", "adjacency");
            var position = this.cellIdToPoint(cellId);

            var neighbors = [];
            for (var i = 0; i < directions.length; ++i) {
                neighbors[i] = this.cells[this.pointToCellId(this.shiftPoint(position, directions[i]))];
            }
            return neighbors;
        },

        countAdjacentEnemyUnits: function (cellId) {
            var unit = this.cells[cellId].unit;
            var neighbors = this.getAdjacentCellsTo(cellId);

            var result = 0;
            for (var i = 0; i < neighbors.length; ++i) {
                if ((neighbors[i].unit)
                    && (neighbors[i].unit.enemyTo(unit))) {
                    ++result;
                }
            }

            return result;
        },
        isVikingUnitsExists: function () {
            var self = this;
            return this.cells.some(function (cell) {
                return self.unitFactory != undefined && self.unitFactory.isViking(cell.unit);
            });
        },

        targetSurroundedUsingThroneRule: function (cellId) {
            if (!this.twoCellsAdjacent(cellId, this.throneLocationId))
                return false;

            var neighbors = this.getAdjacentCellsTo(cellId);

            var unit = this.cells[cellId].unit;
            var uAreDefender = this.cells[this.princeLocationId].unit.alliedWith(unit);
            var throneEmpty = (this.cells[this.throneLocationId].unit === false);
            if ((uAreDefender && throneEmpty) || (!uAreDefender)) {
                if ((this.cellFactory.isThrone(neighbors[0]) && (neighbors[1].unit && neighbors[1].unit.enemyTo(unit)) )
                    || (this.cellFactory.isThrone(neighbors[1]) && (neighbors[0].unit && neighbors[0].unit.enemyTo(unit)) )
                    || (this.cellFactory.isThrone(neighbors[2]) && (neighbors[3].unit && neighbors[3].unit.enemyTo(unit)) )
                    || (this.cellFactory.isThrone(neighbors[3]) && (neighbors[2].unit && neighbors[2].unit.enemyTo(unit)) )) {	//  This looks weird but should work
                    return true;
                }
            }

            return false;
        },

        validMove: function (origin, destination) {
            var ways = this.getValidMoves(origin);
            return !!xSSSR.arrayContains(destination, ways);


        },

        noWay: function (origin) {
            var ways = this.getValidMoves(origin);
            return ways.length == 0;


        },

        getValidMoves: function (cellId) {
            var unit = this.cells[cellId].unit;
            if (unit === false) {
                return [];
            }

            if (this.unitFactory.isNoble(unit)) {
                return this.getValidRookMoves(cellId, this.nobleMaxMovement);
            }

            if (this.unitFactory.isWarrior(unit)) {
                return this.getValidWarriorMoves(cellId);
            }

            return [];
        },

        getValidWarriorMoves: function (positionId) {
            var ways = this.getValidRookMoves(positionId, this.boardSize);
            ways = this.filterOutNobleOnlyCells(ways);

            return ways;
        },

        getValidRookMoves: function (positionId, limit) {
            var directions = this.config.getOption("board", "adjacency");
            var ways = [];

            var origin = this.cellIdToPoint(positionId);
            var source;
            var target;
            var targetId;
            for (var i = 0; i < directions.length; ++i) {
                source = {x: origin.x, y: origin.y};
                for (var j = 0; j < limit; ++j) {
                    target = this.shiftPoint(source, directions[i]);
                    targetId = this.pointToCellId(target);
                    if (this.canMoveIn(targetId)) {
                        ways.push(targetId);
                        source = target;
                    } else {
                        break;
                    }
                }
            }

            return ways;
        },

        canMoveIn: function (cellId) {
            if (!(cellId in this.cells))
                return false;

            if (!this.cellFactory.isPassable(this.cells[cellId]))
                return false;

            return !this.cells[cellId].unit;


        },

        filterOutNobleOnlyCells: function (moves) {
            var result = [];
            var each;
            for (var key in moves) {
                each = moves[key];

                if (this.cellFactory.isSpecial(this.cells[each]))
                    continue;

                result.push(each);
            }

            return result;
        },

        twoCellsAdjacent: function (cellId1, cellId2) {
            var p1 = this.cellIdToPoint(cellId1);
            var p2 = this.cellIdToPoint(cellId2);
            return (Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y) ) === 1;


        },

        // API methods
        //
        unitIsMine: function (cellId) {
            var unit = this.cells[cellId].unit;
            if (unit !== false)
                return (unit.side == this.activeSide);

            return false;
        },

        vikingsVictory: function (gamestate) {
            return (gamestate <= settings.board.gamestate.PRINCESURROUNDED);
        },
        gameInProcess: function (gamestate) {
            return (gamestate === settings.board.gamestate.ONLINE);
        },
        wardensVictory: function (gamestate) {
            return (gamestate >= settings.board.gamestate.PRINCEESCAPED)
        },
        isStalemate: function (gamestate) {
            return (gamestate == settings.board.gamestate.STALEMATE)
        },
        isInterrupted: function (gamestate) {
            return (gamestate == settings.board.gamestate.INTERRUPTED);
        },
        cellInsideBoard: function (cellId) {
            return this.cellFactory.isPassable(this.cells[cellId]);
        },
        cellIsOrdinary: function (cellId) {
            return this.cellFactory.isOrdinary(this.cells[cellId]);
        },
        cellIsThrone: function (cellId) {
            return this.cellFactory.isThrone(this.cells[cellId]);
        },
        cellIsGoal: function (cellId) {
            return this.cellFactory.isGoal(this.cells[cellId]);
        },
        unitIsPrince: function (unit) {
            return this.unitFactory.isPrince(unit);
        },
        unitIsViking: function (unit) {
            return this.unitFactory.isViking(unit);
        },
        unitIsWarden: function (unit) {
            return this.unitFactory.isWarden(unit);
        },

        cellIdToPoint: function (cell) {
            return {x: cell % (this.fieldSize), y: parseInt(cell / (this.fieldSize))}
        },

        xyToCellId: function (x, y) {
            return x + (y * (this.fieldSize) );
        },

        pointToCellId: function (point) {
            return this.xyToCellId(point.x, point.y);
        },

        shiftPoint: function (coord, shift) {
            return {x: coord.x + shift.x, y: coord.y + shift.y};
        }
        // End of API methods
    };

}());