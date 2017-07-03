package server.logger;


import entity.Side;
import generated.GameMechanicRequest;
import generated.GameMechanicResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import server.model.PlayerId;

/**
 * Created by Irina
 */
//TODO: separate to load-time weaver
privileged public aspect LogEvent {
    private static final Logger logger = LogManager.getLogger(LogEvent.class);

    before(SimpMessageHeaderAccessor simpMessageHeaderAccessor, GameMechanicRequest gameMechanicRequest):execution(* server.controllers.TavleiServerController.handleGame(SimpMessageHeaderAccessor, GameMechanicRequest) ) && args(simpMessageHeaderAccessor, gameMechanicRequest)
            {
                logger.debug("Getting request " + gameMechanicRequest.getType() + " from " + simpMessageHeaderAccessor.getSessionId());
            }
    before(String recipient, GameMechanicResponse response):call(* SimpMessagingTemplate.convertAndSend(..) ) && args(recipient, response)
            {
                logger.debug("Sending response " + response.getType() + " to " + recipient);
            }
    after(PlayerId playerId, Side side):execution(* server.model.GamePartyModel.compositeGameParty(PlayerId, Side) ) && args(playerId, side)
            {
                logger.trace("Composite game finish, side " + side + " to " + playerId);
            }
    before(PlayerId playerId, Side side):execution(* server.model.GamePartyModelImpl.newGameParty(PlayerId, Side) ) && args(playerId, side)
            {
                logger.trace("Empty game party creation, for side " + side + ", " + playerId);
            }
    /*
    before():cflowbelow(execution(* GamePartyModel.compositeGameParty(..) )) && !within(LogEvent)
    {
        logger.trace("tracing compositeGame concurency:"+thisJoinPoint);
    }*/

}