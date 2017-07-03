var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.CoordinatorService = function (master, config) {
        this.SERVICEREQUEST = [
            {
                name: "chronicler",
                callback: this.setChronicler.bind(this)
            },
            {
                name: "designer",
                callback: this.setInterface.bind(this)
            },
            {
                name: "horologist",
                callback: this.setTimer.bind(this)
            },
            {
                name: "mediator",
                callback: this.setProxy.bind(this)
            },
            {
                name: "AFTERALL",
                callback: this.allSetAndDone.bind(this)
            }
        ];

        this.master = master;
        this.id = master.getNextId();

        this.config = config;
        this.events = config.getOptionGroup("events");

        this.timerManager = undefined;
        this.eventManager = undefined;
        this.ifaceService = undefined;
        this.proxyService = undefined;

        this.content = {};
        this.content.playingAs = false;

        this.content.board = undefined;		// Waiting for Event Manager
        this.content.selectedCellId = false;
        this.content.currentCellId = false;

        this._process = function () {
            alert("not implemented yet")
        };
        this.isStopped = false;

        return this;
    };

    xSSSR.CoordinatorService.prototype = {


        onBoardEventStateChanged: function (state) {
            //this.ifaceService.debugOut("New game state " + state);
            this.content.selectedCellId = false;
            this.content.currentCellId = false;
            this.pause();
        },

        onBoardEventUnitKilled: function (cellId) {
            //this.ifaceService.debugOut("Unit killed at " + cellId);
            //xSSSR.logMessage("Unit killed at " + cellId);
        },

        onBoardEventUnitMoved: function (from, to) {
            //this.ifaceService.debugOut("Unit moved from " + from + " to " + to);
        },

        onBoardMapLoaded: function (name) {
            this.ifaceService.debugOut("Loaded map " + name);
        },

        allSetAndDone: function () {
            this.eventManager.listen(this.events.byBoard.mapLoaded, this.onBoardMapLoaded.bind(this));
            this.eventManager.listen(this.events.byBoard.unitMoved, this.onBoardEventUnitMoved.bind(this));
            this.eventManager.listen(this.events.byBoard.unitCapturedAt, this.onBoardEventUnitKilled.bind(this));

            this.eventManager.listen(this.events.byBoard.gameStateChangedTo, this.onBoardEventStateChanged.bind(this));
            this.eventManager.listen(this.events.byBoard.gameStateChangedTo, this.ifaceService.shadeBoard.bind(this.ifaceService));

            this.content.board = new xSSSR.GameBoard(this.master, this.config, this.eventManager);
            this.ifaceService.setBoard(this.content.board);

            this.initMainLoop();
            this.timerManager.register("gameCycle", 15, this._iteration.bind(this));
        },
        getBoard: function () {
            return this.content.board;
        },
        setTimer: function (service) {
            this.timerManager = service;
        },
        setChronicler: function (service) {
            this.eventManager = service;
        },

        setInterface: function (service) {
            this.ifaceService = service;
            this.ifaceService.setCallbacks(
                {
                    selectNmove: this.selectNmove.bind(this),
                    deselect: this.deselect.bind(this),
                    highlight: this.highlight.bind(this)
                });
        },

        setProxy: function (service) {
            this.proxyService = service;
            this.proxyService.setCallbacks(
                {
                    selectNmove: this.selectNmove.bind(this),
                    deselect: this.deselect.bind(this)
                });
        },

        initMainLoop: function () {
            // Switch control to remote server only if it's not our turn
            if (this.content.playingAs) {
                if (this.content.board.activeSide !== this.content.playingAs) {
                    this._process = this.proxyService.process.bind(this.proxyService);
                } else {
                    this._process = this.ifaceService.process.bind(this.ifaceService);
                }
            } else {
                this._process = this.ifaceService.process.bind(this.ifaceService);
            }
        },

        play: function () {
            this.timerManager.play("gameCycle");
        },

        pause: function () {
            this.timerManager.pause("gameCycle");
        },

        stop: function () {
            this.isStopped = true;
        },
        start: function () {
            this.isStopped = false;
        },
        _iteration: function () {
            this.ifaceService.stopTracking();

            //this.ifaceService.process();
            if (!this.isStopped) {
                this._process();
                this.ifaceService.fill(this.prepareData());
                this.ifaceService.displayCurrentScene();
                this.ifaceService.resumeTracking();
            }
        },

        prepareData: function () {
            var pattern = false;
            var units = [];
            var result = {};
            var board = this.content.board;
            var state = board.getGameState();
            var stateconf = this.config.getOption("board", "gamestate");
            var sidesconf = this.config.getOption("map", "side");

            result.activeSide = board.activeSide;
            result.gameFinished = false;
            if (board.gameInProcess(state)) {
                result.gameStatus = "Game in process. ";
                if (xSSSR.blocked) {
                    result.gameStatus = "Wait for other player.";
                }
                if (result.activeSide === sidesconf.ATTACKER) {
                    result.gameStatus += " It's vikings turn.";
                } else {
                    result.gameStatus += " It's wardens turn.";
                }
            } else {
                result.gameFinished = true;
                //xSSSR.mode=GameMechanic.GameModeType.PLAY_FROM_ONE_COMPUTER;
                if (board.vikingsVictory(state)) {
                    result.gameStatus = "Vikings victory! ";
                    if (state === stateconf.PRINCESTUCKINTHRONEROOM) result.gameStatus += "Prince was stuck in the Throne room. ";
                    else if (state === stateconf.PRINCEBLOCKED) result.gameStatus += "Prince was blocked near Throne room. ";
                    else if (state === stateconf.PRINCESURROUNDED) result.gameStatus += "Prince was surrounded and now taken by enemy. ";

                }
                if (board.wardensVictory(state)) {
                    result.gameStatus = "Wardens victory!";
                    if (result.state == stateconf.VIGINGSDEFEATED) result.gameStatus += "All vikings was defeated!";
                    else if (result.state == stateconf.PRINCEESCAPED) result.gameStatus += "The prince is free!";

                }
                if (board.isInterrupted(state)) {
                    result.gameStatus = "The game was interrupted!";
                }
                if (board.isStalemate(state)) {
                    result.gameStatus = "Stalemate!";
                }
            }

            result.selectedCell = this.content.selectedCellId;
            result.currentShadow = false;
            result.currentCell = this.content.currentCellId;
            result.currentTurn = board.currentTurn;

            for (var i = 0; i < board.cells.length; ++i) {
                var cell = board.cells[i];
                if (cell.unit !== false) {
                    if (board.unitIsPrince(cell.unit)) pattern = "prince";
                    else if (board.unitIsViking(cell.unit)) pattern = "viking";
                    else if (board.unitIsWarden(cell.unit)) pattern = "warden";

                    if (i === this.content.selectedCellId) {
                        result.currentShadow = pattern;
                    }

                    units.push({cellId: i, pattern: pattern});
                }
            }
            result.units = units;
            return result;
        },

        make_move: function (fromId, toId) {
            this.content.board.move(fromId, toId);
            this.proxyService.move(fromId, toId);
            this.content.selectedCellId = false;
            this.proxyService.deselect();
            this.passTurn();
        },
        selectNmove: function (cellId) {
            if (xSSSR.sideAdapter(this.content.board.activeSide) != xSSSR.mySide && xSSSR.mode != GameMechanic.GameModeType.PLAY_FROM_ONE_COMPUTER) return;
            if (this.content.selectedCellId === cellId) {
                this.content.selectedCellId = false;
                this.proxyService.deselect();
                return;
            }

            if (this.content.board.unitIsMine(cellId)) {
                this.content.selectedCellId = cellId;
                this.proxyService.select(cellId);
                return;
            }

            if (this.content.selectedCellId === false) {
                return false;
            }

            if (this.content.board.validMove(this.content.selectedCellId, cellId)) {
                this.content.board.move(this.content.selectedCellId, cellId);
                this.proxyService.move(this.content.selectedCellId, cellId);
                xSSSR.gameControll.onMakeTurn(xSSSR.PositionFromHash(this.content.selectedCellId), xSSSR.PositionFromHash(cellId));
                this.content.selectedCellId = false;
                this.proxyService.deselect();
                this.passTurn();
            } else {
                // Notify - invalid movement
            }

        },

        deselect: function (cellId) {
            this.content.selectedCellId = false;
            this.proxyService.deselect();
        },

        highlight: function (cellId) {
            this.content.currentCellId = false;
            if (this.content.selectedCellId === false) {
                if (this.content.board.unitIsMine(cellId)) {
                    this.content.currentCellId = cellId;
                    return true;
                }
            } else {
                if (this.content.board.validMove(this.content.selectedCellId, cellId)) {
                    this.content.currentCellId = cellId;
                    return true;
                }
            }

            return false;
        },

        restartBoard: function () {
            this.start();
            this.content.board.restart();
            this.play();
        },

        passTurn: function () {
            //var startPosition=this.board.cellIdToPoint()
            //xSSSR.gameControll.onMakeTurn(gameControll.Position(this.content.board.cellIdToPoint()),gameControll.Position)
            if (this.content.playingAs == undefined)
                return;

            var activeSide = this.content.board.activeSide;
            if (activeSide == this.content.playingAs && typeof this.ifaceService._process === "function") {
                this._process = this.ifaceService.process();
                this.content.board.READONLY = false;
            } else if (typeof this.proxyService._process === "function") {
                this.content.board.READONLY = true;
                this._process = this.proxyService.process();
            }
        }
    };

}());