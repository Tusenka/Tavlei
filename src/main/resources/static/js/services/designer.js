var xSSSR = xSSSR || {};

(function () {
    "use strict";

    xSSSR.InterfaceService = function (master, config) {
        this.SERVICEREQUEST = [
            {
                name: "artist",
                callback: this.setPainter.bind(this)
            }
        ];

        this.TEMPFIELD = "";

        this.master = master;
        this.id = master.getNextId();

        this.config = config;
        this.constants = config.getOptionGroup("interface");

        this.painterService = undefined;

        this.lastAction = "ignore";
        this.mouseControl = {enabled: false, pos: {x: 0, y: 0}, lClick: false, rClick: false, continuedClick: false};
        this.previousLocation = undefined;
        this.callbacks = undefined;

        this.initAreas();
        this.initPatterns();

        return this;
    };

    xSSSR.InterfaceService.prototype = {

        debugOut: function (message) {
            this.painterService.drawDebugText(message + "  " + this.TEMPFIELD);
            this.TEMPFIELD = message;
        },

        debugOutLn: function (message) {
            this.debugOut(message);
            this.TEMPFIELD = "";
        },

        setPainter: function (service) {
            this.painterService = service;
            this.initScene();
        },
        setCallbacks: function (struct) {
            this.callbacks = struct;
        },

        initAreas: function () {
            var canvasSize = this.constants["canvasSize"];
            var canvasZindex = this.constants["layers"].background;

            this.clearAreas();
            this.addArea(new xSSSR.Area("background", 0, 0, canvasSize.w, canvasSize.h));
        },

        initPatterns: function () {//"14px serif"
            this.patterns = {
                background: new xSSSR.InterfacePattern("background", "background", false, false, false, false),
                board: new xSSSR.InterfacePattern("board", "board", false, false, false, false),

                finishingOverlay: new xSSSR.InterfacePattern("cellsBackground", "boardInactive", false, false, false, false),
                cellsBackground: new xSSSR.InterfacePattern("cellsBackground", "cellsBackground", false, false, false, false),

                ordinaryCell: new xSSSR.InterfacePattern(false, false, "shade", 2, false, false),
                throneCell: new xSSSR.InterfacePattern(false, "throneCell", false, 2, false, false),
                exitCell: new xSSSR.InterfacePattern(false, "goalCell", false, 2, false, false),

                frame: new xSSSR.InterfacePattern(false, "frame2", false, 0, false, false),
                shining: new xSSSR.InterfacePattern(false, "shining", false, 3, false, false),

                princeUnit: new xSSSR.InterfacePattern(false, "princeUnit", false, 2, false, false),
                wardenUnit: new xSSSR.InterfacePattern(false, "wardenUnit", false, 2, false, false),
                vikingUnit: new xSSSR.InterfacePattern(false, "vikingUnit", false, 2, false, false),
                princeUnitShade: new xSSSR.InterfacePattern(false, "princeUnitShade", false, 2, false, false),
                wardenUnitShade: new xSSSR.InterfacePattern(false, "wardenUnitShade", false, 2, false, false),
                vikingUnitShade: new xSSSR.InterfacePattern(false, "vikingUnitShade", false, 2, false, false)
            };
        },

        initScene: function () {
            var layers = this.constants["layers"];

            this.scene = {
                background: new xSSSR.InterfaceObject(this.zones.background, this.patterns.background, layers.background, false),
                selection: new xSSSR.InterfaceObject(this.zones.background, this.patterns.frame, layers.arrows, false),
                unitShadow: new xSSSR.InterfaceObject(this.zones.background, this.patterns.princeUnitShade, layers.figures, false)
            };

            this.displayObject(this.scene.background);
        },

        shadeBoard: function (message) {
            if (true || message == "") {
                this.scene.cellsBackgroundOverlay.hide();
            } else {
                this.scene.cellsBackgroundOverlay.show();
            }
        },

        setBoard: function (board) {
            var size = board.boardSize;
            var upperLeftCorner = board.upperLeftCorner;
            var cellSize = this.constants.cellSize;
            var boardZindex = this.constants.layers.board;
            var drawingsZindex = this.constants.layers.drawings;
            var overlayZindex = this.constants.layers.overlay;
            var cellZindex = this.constants.layers.cells;
            var boardPadding = this.constants.boardPadding;
            var boardPosition = this.constants.boardPosition;
            var cellsPosition = {x: boardPosition.x + boardPadding.x, y: boardPosition.y + boardPadding.y};
            var canvasX = 0;
            var canvasY = 0;
            var cellNames = [];
            var cellName;
            var pattern;

            this.addArea(new xSSSR.Area("board", boardPosition.x, boardPosition.y, cellSize * size + 2 * boardPadding.x, cellSize * size + 2 * boardPadding.y));
            this.scene.board = new xSSSR.InterfaceObject(this.zones.board, this.patterns.board, boardZindex, false);

            this.addArea(new xSSSR.Area("cellsBackground", cellsPosition.x, cellsPosition.y, cellSize * size, cellSize * size));
            this.scene.cellsBackground = new xSSSR.InterfaceObject(this.zones.cellsBackground, this.patterns.cellsBackground, drawingsZindex, false);
            this.scene.cellsBackgroundOverlay = new xSSSR.InterfaceObject(this.zones.cellsBackground, this.patterns.finishingOverlay, overlayZindex, false);
            this.scene.cellsBackgroundOverlay.hide();

            for (var y = 0; y < size; ++y) {
                for (var x = 0; x < size; ++x) {
                    var cellId = board.xyToCellId(x + upperLeftCorner.x, y + upperLeftCorner.y);
                    if (!(board.cellInsideBoard(cellId))) {
                        continue;
                    }

                    if (board.cellIsOrdinary(cellId)) pattern = this.patterns.ordinaryCell;
                    else if (board.cellIsThrone(cellId)) pattern = this.patterns.throneCell;
                    else if (board.cellIsGoal(cellId)) pattern = this.patterns.exitCell;

                    cellName = "cell_" + cellId;
                    cellNames.push(cellName);
                    this.addArea(new xSSSR.Area(cellName, cellsPosition.x + x * cellSize, cellsPosition.y + y * cellSize, cellSize, cellSize));

                    var object = new xSSSR.InterfaceObject(this.zones[cellName], pattern, cellZindex, cellId);
                    this.scene[cellName] = object;
                }
            }

            this.createObjectsGroup("boardCells", cellNames);
        },

        // API
        displayCurrentScene: function () {
            this.displayObject(this.scene.background);
            this.displayObject(this.scene.cellsBackground);
            this.displayObject(this.scene.board);
            this.displayGroup("boardCells");
            this.displayObject(this.scene.unitShadow);
            this.displayGroup("boardUnits");
            this.displayObject(this.scene.selection);
            this.displayObject(this.scene.cellsBackgroundOverlay);

            //this.displayGroup("HUD"); 			
            //this.displayGroup("controls"); 			
            if (this.constants.showDebugLayer) {
                //this.displayGroup("debug"); 			
            } else {
                //this.displayGroup("overlay"); 			
            }
        },

        displayGroup: function (groupName) {
            var object;
            if (!(groupName in this.groups)) {
                throw "InterFace: unknown interface objects group " + groupName;
            }
            var objectsInGroup = this.groups[groupName];
            var key;
            for (var i = 0; i < objectsInGroup.length; ++i) {
                key = objectsInGroup[i];
                object = this.scene[key];
                this.displayObject(object);
            }
        },

        displayObject: function (object) {
            this._display(object.area, object.pattern, object.properties);
        },

        _display: function (location, pattern, properties) {
            if (properties && (!properties.visible)) {
                return false;
            }

            if (pattern.padding) {
                this.painterService.drawPaddedArea(location, this.getStyle(pattern.style), pattern.padding);
            } else {
                this.painterService.drawArea(location, this.getStyle(pattern.style));
            }

            if (pattern.border) {
                this.painterService.drawAreaBorder(location, this.getStyle("cellBorders"), pattern.border);
            }

            if (!properties)
                return true;

            if (pattern.hovered && properties.hovered) {
                //this.painterService.drawAreaOutline(location, this.getStyle(pattern.hovered), 1);
                this.painterService.drawPaddedArea(location, this.getStyle(pattern.hovered), 2);
            }

            if (pattern.text) {
                if (properties.text) {
                    this.painterService.drawAreaTextFont(location, this.getStyle("defaultText"), properties.text, pattern.text);
                } else {
                    this.painterService.drawAreaTextFont(location, this.getStyle("defaultText"), pattern.text, pattern.text);
                }
            }

            return true;
        },

        displayCustom: function (locationName, patternName, properties) {
            if (!(patternName in this.patterns)) {
                throw "Interface: unknown graphical interface pattern " + patternName;
            }

            var pattern = this.patterns[patternName];
            var location = this.getAreaByName(locationName);

            if ((pattern.target) && (pattern.target !== locationName)) {
                throw "Interface: incorrect placement for pattern " + patternName + " into area " + locationName;
            }

            this._display(location, pattern, properties);
        },

        objectVisible: function (name) {
            if (!(name in this.scene))
                return false;

            return this.scene[name].isVisible();
        },

        getStyle: function (styleName) {
            var external = this.config.getOptionGroup("drawing")["externalResources"];
            var palette = this.config.getOptionGroup("drawing")["palette"];
            if ((styleName in palette) && !(styleName in external)) {
                return palette[styleName];
            }
            return styleName;
        },

        // API
        stopTracking: function () {
            this.lastAction = this.mouseAction();

            this.doNotTrackMouse = true;
            this.mouseControl.enabled = false;
        },

        // API
        resumeTracking: function () {
            this.mouseControl.enabled = true;
            this.doNotTrackMouse = false;
        },

        // API
        process: function (object) {
            var action = this.lastAction;
            var locationName = this.mouseLocation();
            if (!locationName) {
                return false;
            }
            var location = this.getObjectByName(locationName);

            switch (action) {
                case "click":
                    xSSSR.logMessage(action + "@" + locationName + " CELL CLICK DETECTED");
                    if (this.objectInGroup(locationName, "boardCells")) {
                        this.callbacks.selectNmove(location.text());
                        //this.debugOut(action + "@" + locationName + " CELL CLICK DETECTED");
                    }
                    break;
                case "hover":
                    if (this.objectInGroup(locationName, "boardCells")) {
                        var isHovered = this.callbacks.highlight(location.text());
                        if (!isHovered)
                            break;
                    }

                    if ((this.previousLocationName) && (this.previousLocationName !== locationName)) {
                        var previousLocation = this.getObjectByName(this.previousLocationName);
                        previousLocation.properties.hovered = false;
                    }
                    this.previousLocationName = locationName;

                    //this.debugOut(action + "@" + locationName + "#");
                    location.properties.hovered = true;
                    break;
                case "ignore":
                    break;
                default:
                    throw "Interface: unknown action sequence " + action;
            }

            return true;
        },
        showMessage: function (message) {
            var props = this.scene.board.properties;
            props.gameStatus = message;
            this.debugOutLn(props.gameStatus);
        },
        // API
        fill: function (information) {
            var unitZindex = this.constants["layers"].figures;
            var cellName;
            var props = this.scene.board.properties;
            this.showMessage(information.gameStatus);

            if (information.gameFinished) {
                this.shadeBoard(information.gameStatus);
                //TODO: Shade the last-turned piece
                //return;
            } else {
                this.shadeBoard("");
            }

            props.selectedCell = information.selectedCell;
            if (props.selectedCell) {
                cellName = "cell_" + props.selectedCell;
                this.scene.selection.area = this.zones[cellName];
                this.scene.selection.properties.visible = true;
            } else {
                this.scene.selection.properties.visible = false;
            }

            props.currentShadow = information.currentShadow;
            props.currentCell = information.currentCell;
            if (props.currentCell) {
                cellName = "cell_" + props.currentCell;
                this.scene.unitShadow.area = this.zones[cellName];
                if (props.currentShadow) {
                    this.scene.unitShadow.pattern = this.patterns[props.currentShadow + "UnitShade"];
                } else {
                    this.scene.unitShadow.pattern = this.patterns["shining"];
                }
                this.scene.unitShadow.properties.visible = true;
            } else {
                this.scene.unitShadow.properties.visible = false;
            }

            props.activeSide = information.activeSide;
            props.currentTurn = information.currentTurn;
            props.units = information.units;
            var unitNames = [];
            this.clearObjectsGroup("boardUnits");
            for (var i = 0; i < props.units.length; ++i) {
                var unit = props.units[i];
                cellName = "cell_" + unit.cellId;
                var unitName = "unit_" + unit.cellId;

                var object = new xSSSR.InterfaceObject(this.zones[cellName], this.patterns[unit.pattern + "Unit"], unitZindex, false);

                this.scene[unitName] = object;
                unitNames.push(unitName);
            }
            this.createObjectsGroup("boardUnits", unitNames);
            this.scene.board.properties = props;
        },

        mouseAction: function () {
            if (!this.mouseControl.enabled)
                return "ignore";

            if (this.mouseControl.continuedClick) {
                // mouse button still pressed
                //return "ignore";
            }

            var left = this.mouseControl.lClick;
            var right = this.mouseControl.rClick;

            this.mouseControl.lClick = false;
            this.mouseControl.rClick = false;

            if (left || right) {
                return "click";
            } else {
                return "hover";
            }
        },


        addArea: function (area) {
            this.zones[area.name] = area;
        },

        createObjectsGroup: function (groupName, sceneObjectNames) {
            for (var i = 0; i < sceneObjectNames.length; ++i) {
                this.objectToGroup[sceneObjectNames[i]] = groupName;
            }

            this.groups[groupName] = sceneObjectNames;
        },

        clearObjectsGroup: function (groupName) {
            var objectName;
            if (!(groupName in this.groups))
                return false;

            for (var i = 0; i < this.groups[groupName].length; ++i) {
                objectName = this.groups[groupName][i];
                this.objectToGroup[objectName] = false;
                this.scene[objectName] = false;
            }

            this.groups[groupName] = [];
        },

        objectInGroup: function (objectName, groupName) {
            if (objectName in this.objectToGroup)
                return this.objectToGroup[objectName] === groupName;

            return false;
        },

        areaInGroup: function (area, groupName) {
            return this.objectInGroup(area.name, groupName);
        },

        getAreaByPoint: function (point) {
            var loc = this.getLocationByPoint(point);
            if (!loc)
                return false;

            return loc.area;
        },

        getLocationByPoint: function (point) {
            var result = this.getObjectName(point);
            if (result === false)
                return false;

            return this.scene[result];
        },

        getObjectName: function (point) {
            var index = false;
            var result = false;
            var area;
            var obj;

            for (var key in this.scene) {
                obj = this.scene[key];
                if (!obj)
                    continue;

                area = obj.area;

                if (!this.pointInside(point, area))
                    continue;

                if (index === false) {
                    index = obj.zindex;
                }

                if (obj.zindex >= index) {
                    result = key;
                    index = obj.zindex;
                }
            }

            return result;
        },

        getAreaByName: function (areaName) {
            if (!(areaName in this.zones))
                throw "Interface: cannot find such area: " + areaName;

            return this.zones[areaName];
        },

        getObjectByName: function (locationName) {
            if (!(locationName in this.scene))
                throw "Interface: cannot find such location: " + locationName;

            return this.scene[locationName];
        },

        clearAreas: function () {
            this.scene = {};
            this.zones = {};
            this.groups = {};
            this.objectToGroup = {};
        },

        pointInside: function (point, area) {
            if ((point.x >= area.x1) && (point.y >= area.y1)) {
                if ((point.x <= area.x2 ) && (point.y <= area.y2)) {
                    return true;
                }
            }
            return false;
        },

        mouseLocation: function () {
            return this.getObjectName(this.mouseControl.pos);
        },

        // API function for server.controllers
        trackMouse: function (x, y, leftButton, rightButton, buttonDown) {
            if (this.doNotTrackMouse)
                return;

            if ((this.mouseControl.continuedClick) && (!buttonDown)) {
                this.mouseControl = {
                    enabled: true,
                    pos: {x: x, y: y},
                    lClick: this.mouseControl.lClick,
                    rClick: this.mouseControl.rClick,
                    continuedClick: false
                };
            } else {
                this.mouseControl = {
                    enabled: true,
                    pos: {x: x, y: y},
                    lClick: leftButton,
                    rClick: rightButton,
                    continuedClick: buttonDown
                };
            }

        }
    };

}());