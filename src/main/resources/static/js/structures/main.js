var Master = undefined;
var Plan = undefined;
var settings = {
    server: {
        gateway: "/ward/gate.php"
    },
    system: {
        clockAutoStart: true,
        refresh: 16
    },
    events: {
        byBoard: {
            mapLoaded: 0,
            unitMoved: 1,
            unitCapturedAt: 2,
            gameStateChangedTo: 3
        }
    },
    interface: {
        canvasSize: {w: 1024, h: 512},
        cellSize: 45,
        boardPosition: {x: 302, y: 48},
        boardPadding: {x: 6, y: 6},
        showDebugLayer: false,
        layers: {
            drawings: -32,
            overlay: -16,
            hud: -8,
            figures: -2,
            arrows: -1,
            background: 0,
            board: 1,
            cells: 2,
            controls: 9
        }
    },
    drawing: {
        charactersInOneLine: 4,
        externalResources: {
            background: "http://tavlei.net/game/static/black_round.png",
            board: "http://tavlei.net/game/static/board_frame.png",
            boardInactive: "http://tavlei.net/game/static/board_inactive_overlay.png",
            cellsBackground: "http://tavlei.net/game/static/board_texture.png",

            shade: "http://tavlei.net/game/static/shade.png",
            shining: "http://tavlei.net/game/static/shining.png",
            frame: "http://tavlei.net/game/static/frame.png",
            frame2: "http://tavlei.net/game/static/frame2.png",

            throneCell: "http://tavlei.net/game/static/defence.png",
            goalCell: "http://tavlei.net/game/static/target.png",

            princeUnit: "http://tavlei.net/game/static/knyaz.png",
            wardenUnit: "http://tavlei.net/game/static/shield.png",
            vikingUnit: "http://tavlei.net/game/static/axe2.png",
            princeUnitShade: "http://tavlei.net/game/static/knyaz_shadow.png",
            wardenUnitShade: "http://tavlei.net/game/static/shield_shadow.png",
            vikingUnitShade: "http://tavlei.net/game/static/axe2_shadow.png"
        },
        palette: {
            background: "#000000",
            disabledCell: "#EEEEEE",
            cellBorders: "#808000",

            ordinaryCell: "#D6A400",
            throneCell: "#00B32D",
            exitCell: "#FF0000",

            ordinaryCellHover: "#C7930F",
            throneCellHover: "#08AB31",
            exitCellHover: "#F50A0A",

            validMoveIngame: "#2693FF",
            validMoveEndgame: "#007FFF",

            numbersInside: "#400010",
            defaultText: "#000000"
        }
    },
    board: {
        adjacency: [
            {x: 1, y: 0},
            {x: -1, y: 0},
            {x: 0, y: 1},
            {x: 0, y: -1}
        ],

        gamestate: {
            PRINCESTUCKINTHRONEROOM: -3,
            PRINCEBLOCKED: -2,
            PRINCESURROUNDED: -1,
            ONLINE: 1,
            STALEMATE: 2,
            INTERRUPTED: 3,
            PRINCEESCAPED: 100,
            VIGINGSDEFEATED: 200,
        },

        floor: {
            DISABLED: 0,
            NORMAL: 1,
            THRONE: 2,
            EXIT: 3
        }
    },
    map: {
        unitType: {
            PRINCE: "prince",
            WARDEN: "warden",
            VIKING: "viking"
        },

        unitRank: {
            NOBLE: 0,
            WARRIOR: 1
        },

        side: {
            DEFENDER: 0,
            ATTACKER: 1
        }
    }
};
xSSSR.getBoard = null;
xSSSR.mode = GameMechanic.GameModeType.PLAY_FROM_ONE_COMPUTER;
xSSSR.mySide = null;
xSSSR.blocked = false;
xSSSR.block = function () {
    xSSSR.blocked = true;
};
xSSSR.unlock = function () {
    xSSSR.blocked = false;
};
function startEngine() {
    var gMaster = xSSSR.getGrandMaster();

    // Inform game master about working services
    // Start with config
    var gConfig = new xSSSR.ConfigService(gMaster);
    gConfig.load(settings);

    gMaster.addSlave("configurer", gConfig);
    // Other "http://tavlei.net/game/static guys
    gMaster.addResource("canvas", document.getElementById("focusedView"));
    gMaster.addResource("overview", document.getElementById("overallView"));

    // ... dynamic too
    gMaster.addSlave("artist", new xSSSR.DrawingService(gMaster, gConfig));
    gMaster.addSlave("chronicler", new xSSSR.EventManager(gMaster, gConfig));
    gMaster.addSlave("server.controllers", new xSSSR.MouseController(gMaster, gConfig));
    gMaster.addSlave("coordinator", new xSSSR.CoordinatorService(gMaster, gConfig));	// Game logic keeper
    gMaster.addSlave("designer", new xSSSR.InterfaceService(gMaster, gConfig));
    gMaster.addSlave("horologist", new xSSSR.TimerService(gMaster, gConfig));
    gMaster.addSlave("mediator", new xSSSR.ProxyService(gMaster, gConfig));


    // Ready to work...
    gMaster.wellDone();		// ...if all dependencies in slave army solved
    // crash if not
    xSSSR.Master = gMaster;
    Plan = gConfig;

    xSSSR.getBoard = function () {
        return xSSSR.Master.getSlave("coordinator").getBoard();
    };

    xSSSR.Master.getSlave("horologist").start();
    xSSSR.socket.connect();


}

function restartGamePlease() {
    xSSSR.Master.getSlave("coordinator").restartBoard();
    xSSSR.Master.getSlave("coordinator").start();
    xSSSR.mode = GameMechanic.GameModeType.PLAY_FROM_ONE_COMPUTER;
    xSSSR.mySide = getMySide();
    xSSSR.unlock();

}
function playWithAI() {
    xSSSR.Master.getSlave("coordinator").restartBoard();
    xSSSR.Master.getSlave("coordinator").start();
    xSSSR.mode = GameMechanic.GameModeType.PLAY_WITH_AI;
    xSSSR.mySide = getMySide();
    xSSSR.socket.startGame(xSSSR.mode, xSSSR.mySide);
}
function playWithHuman() {
    xSSSR.Master.getSlave("coordinator").restartBoard();
    xSSSR.Master.getSlave("coordinator").start();
    xSSSR.mode = GameMechanic.GameModeType.MULTILAYER;
    xSSSR.mySide = getMySide();
    xSSSR.socket.startGame(xSSSR.mode, xSSSR.mySide);
}

function getMySide() {
    switch ($("input[name='mySide']:checked").val()) {
        case "BLACK":
            return GameMechanic.Side.BLACK;
            break;
        case "WHITE":
            return GameMechanic.Side.WHITE;
            break;
        default:
            return null;
    }
}
function showMain() {
    //document.getElementById("focusedView").style.visibility = 'visible';
    //document.getElementById("auxScreen").style.visibility = 'hidden';
    //toggleButtonStates("#mainScreenButton");
}
function toggleButtonStates(active) {
    //$("#mainScreenButton").removeClass("btn-primary");
    //$("#auxScreenButton").removeClass("btn-primary");

    //$(active).addClass("btn-primary");
}
getCoordinator = function () {
    return xSSSR.Master.getSlave("coordinator");
};
getConfig = function () {
    return xSSSR.Master.getSlave("coordinator").config;
};