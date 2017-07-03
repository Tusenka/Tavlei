/**
 * Created by Irina on 26.12.2016.
 */
var xSSSR = xSSSR || {};
(function () {
    "use strict";

    xSSSR.gameControll = {
        makeTurn: function (startPosition, endPosition) {
            startPosition = xSSSR.toPosition(startPosition);
            endPosition = xSSSR.toPosition(endPosition);
            var fromId = xSSSR.getBoard().xyToCellId(startPosition.col + 1, startPosition.row + 1);
            getCoordinator().content.playingAs = xSSSR.sideToJsSide(xSSSR.mySide);
            getCoordinator().make_move(fromId, xSSSR.getBoard().xyToCellId(endPosition.col + 1, endPosition.row + 1));
        },
        onMakeTurn: function (startPosition, endPosition) {
            try {
                if (xSSSR.mode == GameMechanic.GameModeType.PLAY_FROM_ONE_COMPUTER) return;
                startPosition = xSSSR.toPosition(startPosition);
                endPosition = xSSSR.toPosition(endPosition);
                var move = new xSSSR.Move(startPosition, endPosition);
                xSSSR.logMessage("Unit moved " + move);
                xSSSR.socket.sendMove(move);
            }
            catch (e) {
                console.log(e);
            }
            //this.makeTurn({x:7,y:5},{x:7,y:3});
        },
        endGame: function (sideWinner, isStalemate) {
            var winStatus;
            if (isStalemate) {
                winStatus = this.config.getOption("board", "gamestate").STALEMATE;
            }
            else {
                if (sideWinner == GameMechanic.Side.BLACK) winStatus = settings.board.gamestate.PRINCESURROUNDED;
                if (sideWinner == GameMechanic.Side.WHITE) winStatus = settings.board.gamestate.PRINCEESCAPED;
            }
            getConfig()._setOption("board", "gamestate", winStatus);
        },
        interruptGame: function (reason) {
            getCoordinator().stop();
            reason = reason || "The game was interrupted!";
            xSSSR.gameControll.showMessage(reason);
            getConfig()._setOption("board", "gamestate", settings.board.gamestate.INTERRUPTED);
            //xSSSR.mode=GameMechanic.GameModeType.PLAY_FROM_ONE_COMPUTER;

        },
        showMessage: function (message) {
            getCoordinator().ifaceService.showMessage(message);
        },


    }


}());
