/**
 * Created by Irina on 03.01.2017.
 */
var xSSSR = xSSSR || {};
(function () {
    "use strict";

    xSSSR.socket = {
        socketJs: null,
        stompClient: null,
        getSessionIdFromSocket: function (socket) {
            var url = socket._transport.url; // "http://localhost:8080/user/482/0bb13347/websocket"
            var result = url.match(/.*\/([^\/]*)\/websocket$/);
            return result[1];
        },
        connect: function () {
            this.socketJs = new SockJS('/move');
            this.stompClient = Stomp.over(this.socketJs);
            var outer = this;
            this.stompClient.onDiscontectListener = function () {

                if (xSSSR.mode != GameMechanic.GameModeType.PLAY_FROM_ONE_COMPUTER) {
                    xSSSR.gameControll.interruptGame("Lost connection to the server!");
                }

            };
            this.stompClient.connect({}, function (frame) {
                this.subscribe('/result/move/' + outer.getSessionIdFromSocket(outer.socketJs), function (answer) {
                    outer.serverGameResponse(answer.body);
                });

            });
        },

        sendMove: function (move) {
            var request = new GameMechanic.GameMechanicRequest();
            request.type = GameMechanic.RequestType.MAKE_TURN;
            request.move = move;
            this.stompClient.send("/app/move", {}, JSON.stringify(request));
        },
        startGame: function (gameMode, side) {
            var request = new GameMechanic.GameMechanicRequest();
            request.type = GameMechanic.RequestType.GAME_BEGIN;
            request.mode = gameMode;
            request.mySide = side;
            if (gameMode != GameMechanic.GameModeType.PLAY_FROM_ONE_COMPUTER) {
                if (!this.stompClient.connected) {
                    xSSSR.gameControll.interruptGame("Can't connect to the server!");
                }
                xSSSR.block();
                this.stompClient.send("/app/move", {}, JSON.stringify(request));
            }
        },
        serverGameResponse: function (data) {
            data = JSON.parse(data);
            var response = new GameMechanic.GameMechanicResponse();
            xSSSR.convertObject(data, response);
            if (response.type == GameMechanic.ResponseType.PARTNER_FOUNDED) {
                xSSSR.mySide = response.yourSide;
                xSSSR.unlock();
                alert("Game begin. Your side:" + xSSSR.sideToString(xSSSR.mySide) + "!");
                $('input:radio[name="mySide"]').filter('[value="' + xSSSR.mySide + '"]').attr('checked', true);
            }
            if (response.type == GameMechanic.ResponseType.INTERRUPT_GAME) {
                response.message = response.message || "The game was interrupted!";
                xSSSR.gameControll.interruptGame(response.message);
                xSSSR.unlock();
                alert(response.message);
            }
            if (response.type == GameMechanic.ResponseType.MAKE_TURN) {
                xSSSR.gameControll.makeTurn(response.move.start, response.move.destination);
            }
            if (response.type == GameMechanic.ResponseType.WIN_GAME) {
                console.log("Game end! Winner:" + response.winner);
            }

        },

    };


}());