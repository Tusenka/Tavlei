package server.controllers;


import entity.Move;
import entity.Side;
import entity.TavleiState;
import entity.event.GameMechanicEventType;
import gamemechanics.controller.tavlei.TavleiControllerManager;
import gamemechanics.model.event.*;
import generated.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import server.model.*;

import java.util.Random;


@Controller
@SuppressWarnings("SameReturnValue")
public class TavleiServerController implements ApplicationListener<SessionDisconnectEvent> {
    @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
    @Autowired
    private SimpMessagingTemplate messageTemplate;
    private final GamePartyModel tavleiGamePartyModel = new GamePartyModelImpl(new TavleiControllerManager("fiction"));

    private final String gameIdName = "tavleiPartyId";


    @RequestMapping("/game")
    public String game(Model model) {
        return "game";
    }

    @MessageMapping("/move")
    public void handleGame(SimpMessageHeaderAccessor headerAccessor, GameMechanicRequest request) throws Exception {
       try {
            switch (request.getType()) {
                case GAME_BEGIN:
                    gameBegin(headerAccessor, request);
                    break;
                case MAKE_TURN:
                    makeMove(headerAccessor, request);
                    break;
                case GAME_END:
                    gameEnd(headerAccessor);
                    break;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


    }

    @MessageMapping("/help/move")
    public void handleSession(SimpMessageHeaderAccessor headerAccessor, SessionRequest sessionRequest) throws Exception {
        SessionResponse sessionResponse = new SessionResponse();
        sessionResponse.setYourSessionId(headerAccessor.getSessionId());
        messageTemplate.convertAndSend("/result/move/yourSessionId/" + sessionRequest.getSendTo(), sessionResponse);

    }

    /*
    * On game session end
     */
    public void onApplicationEvent(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        gameEnd(headerAccessor);
   }

    private void gameBegin(SimpMessageHeaderAccessor headerAccessor, GameMechanicRequest request) {
        gameEnd(headerAccessor);
        GameParty gameParty;
        PlayerId playerId = new PlayerId(headerAccessor.getSessionId());
        switch (request.getMode()) {
            case MULTILAYER:
                if (request.getMySide() == null)
                    gameParty = tavleiGamePartyModel.compositeGameParty(playerId);
                else
                    gameParty = tavleiGamePartyModel.compositeGameParty(playerId, request.getMySide());
                break;
            case PLAY_WITH_AI:
                if (request.getMySide() != null)
                    gameParty = tavleiGamePartyModel.newGameParty(playerId, request.getMySide());
                else
                    gameParty = tavleiGamePartyModel.newGameParty(playerId, Side.values()[new Random(System.currentTimeMillis()).nextInt(Side.values().length)]);
                break;
            default:
                throw new UnsupportedOperationException("Game mode " + request.getMode() + " are not supported");
        }
        gameParty.setGameModeType(request.getMode());
        headerAccessor.getSessionAttributes().put(gameIdName, gameParty.getPartyId());
        if (gameParty.getPartyStatus() == PartyStatus.READY) {
            gameParty.start();
            subscribeGameEvents(gameParty, headerAccessor);
            gameParty.getPlayerIds().forEach((side, s) -> {
                        GameMechanicResponse response = new GameMechanicResponse();
                        response.setType(ResponseType.PARTNER_FOUNDED);
                        response.setYourSide(side);
                        messageTemplate.convertAndSend("/result/move/" + s, response);
                    }
          );
        } else {
            notifyPlayers(gameParty, ResponseType.PARTNER_SEARCHING);
        }
    }

    private void gameEnd(SimpMessageHeaderAccessor headerAccessor) {
        GamePartyId gamePartyId = (GamePartyId) headerAccessor.getSessionAttributes().get(gameIdName);
        if (gamePartyId != null) {
            GameMechanicResponse response = new GameMechanicResponse();
            response.setType(ResponseType.INTERRUPT_GAME);
            response.setMessage("Game was interrupted by other player!");
            tavleiGamePartyModel.getGamePartyById(gamePartyId).getPlayerIds().forEach((side, playerId) ->
            {
                if (!playerId.equals(headerAccessor.getSessionId())) {
                    messageTemplate.convertAndSend("/result/move/" + playerId, response);
                }
            });
            //End Game Party and destroy event space with listeners;
            tavleiGamePartyModel.endGameParty(gamePartyId);
            headerAccessor.getSessionAttributes().remove(gameIdName);
        }
    }

    private void makeMove(SimpMessageHeaderAccessor headerAccessor, GameMechanicRequest request) {
        if (headerAccessor.getSessionAttributes().get(gameIdName) == null) {
            request.setMode(GameModeType.PLAY_WITH_AI);
            gameBegin(headerAccessor, request);
        }
        GamePartyId gamePartyId = (GamePartyId) headerAccessor.getSessionAttributes().get(gameIdName);
        PlayerId playerId = new PlayerId(headerAccessor.getSessionId());
        tavleiGamePartyModel.getGamePartyById(gamePartyId).makeMove(playerId, request.getMove());
    }

    @SuppressWarnings("SameParameterValue")
    private void notifyPlayers(GameParty gameParty, ResponseType responseType) {
        GameMechanicResponse response = new GameMechanicResponse();
        response.setType(responseType);
        gameParty.getPlayerIds().forEach((side, s) -> messageTemplate.convertAndSend("/result/move/" + s, response));
    }

    private void notifyPlayers(GameParty gameParty, GameMechanicResponse response) {
        gameParty.getPlayerIds().forEach((side, s) -> messageTemplate.convertAndSend("/result/move/" + s, response));
    }


    private void subscribeGameEvents(GameParty gameParty, SimpMessageHeaderAccessor headerAccessor) {
        EventManager eventManager = EventManager.getEventListenerForMe(gameParty.getEventSpace(), this);
        eventManager.addListener(GameMechanicEventType.BEGIN_TURN, event ->
                {
                    GameMechanicResponse response = new GameMechanicResponse();
                    response.setType(ResponseType.TURN_CONFIRMED);
                    messageTemplate.convertAndSend("/result/move/" + headerAccessor.getSessionId(), response);
                }
        );
        eventManager.addListener(GameMechanicEventType.GAME_OVER, event ->
                {
                    GameMechanicResponse response = new GameMechanicResponse();
                    response.setType(ResponseType.WIN_GAME);
                    response.setIsStalemate(false);
                    switch ((TavleiState)event.getData()) {
                        case WHITE_WINS:
                            response.setWinner(Side.WHITE);
                        case BLACK_WINS:
                            response.setWinner(Side.BLACK);
                        case STALEMATE:
                            response.setIsStalemate(true);
                            break;
                    }
                    notifyPlayers(gameParty, response);
                }
        );
        eventManager.addListener(GameMechanicEventType.MOVE, event ->
                {
                    GameMechanicResponse response = makeMoveResponse((Move) event.getData());
                    gameParty.getPlayerIds().forEach((side, s) ->
                            {
                                if (!side.equals(event.getSourceSide()))
                                    messageTemplate.convertAndSend("/result/move/" + s, response);
                            }
                    );
                }
        );
        eventManager.addListener(GameMechanicEventType.GAME_RULES_ERROR, event ->
                {
                    GameMechanicResponse response = new GameMechanicResponse();
                    response.setType(ResponseType.TURN_UNCONFIRMED);
                    response.setMessage((String) event.getData());
                    messageTemplate.convertAndSend("/result/move/" + headerAccessor.getSessionId(), response);
                }
        );
    }

    private GameMechanicResponse makeMoveResponse(Move move) {
        GameMechanicResponse response = new GameMechanicResponse();
        response.setType(ResponseType.MAKE_TURN);
        response.setMove(move);
        return response;
    }


}
