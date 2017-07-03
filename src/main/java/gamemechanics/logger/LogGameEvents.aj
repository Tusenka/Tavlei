package gamemechanics.logger;


import entity.event.GameEvent;
import entity.event.GameMechanicEventType;
import gamemechanics.model.event.EventManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Created by Irina
 */
privileged public aspect LogGameEvents {
    private static final Logger logger = LogManager.getLogger("LogGameEvents");
    //Prevent bubbling
    before(GameEvent gameEvent, EventManager eventManager): execution(* EventManager.triggerEvent(GameEvent))&& args(gameEvent) && this(eventManager)&&(!cflowbelow( execution(*  EventManager.EventManagerMock.triggerEvent(..) )))
            {
                try {
                    String eventSpace = eventManager.getEventSpace();
                    String holderId = eventManager.holderId;
                    String prefix = "Event space: " + eventSpace + ".";
                    if (gameEvent.getType().equals(GameMechanicEventType.GAME_RULES_ERROR.toString())) {
                        //log game rules broken
                        logger.error(prefix + " GAME RULES ERROR!. " + gameEvent.getData());
                    } else {
                        logger.debug(prefix + " Event " + gameEvent.getType() + ". Source side: " + gameEvent.getSourceSide() + ". Attached information:  " + gameEvent.getData() + ". Holder: " + holderId + ".");
                    }
                }
                //As a logging, any exception mustn't influences to the main execution flow
                catch (Exception e) {
                    logger.fatal("Can't log game event!" + e);
                    //Do not rethrow anything!
                }
            }

}