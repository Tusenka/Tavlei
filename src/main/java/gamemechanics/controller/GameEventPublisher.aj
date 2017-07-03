package gamemechanics.controller;


import entity.GameState;
import entity.Move;
import entity.Side;
import entity.event.GameEvent;
import entity.event.GameMechanicEventType;
import entity.event.TextMessage;
import gamemechanics.model.event.EventManager;
import gamemechanics.model.event.EventSpaceSupplement;
import gamemechanics.*;

/**
 * Created by Irina
 */
public aspect GameEventPublisher {

    //Turn Events+stop bubbling
    before(GameController gameController): execution(* controller.GameController.beginTurn(..))&& !cflowbelow( execution(* controller.GameController.beginTurn(..)) )&& this(gameController)
            {
                Side side = gameController.getMySide();
                triggerEvent(gameController, side, GameMechanicEventType.BEGIN_TURN);
            }
    //after move had made
    after(Move move, GameController gameController):execution(* controller.GameController.makeMove( entity.Move ))&&!cflowbelow( execution(* controller.GameController.makeMove( .. )))&& args(move) && this(gameController)
            {
                Side side = gameController.getMySide();
                GameEvent gameEvent = new GameEvent(GameMechanicEventType.MOVE, move).setSourceSide(side);
                triggerEvent(gameController, gameEvent);
            }
    after(GameController gameController):execution(* controller.GameController.endTurn(..))&& !cflowbelow( execution(* controller.GameController.endTurn(..)))&& this(gameController)
            {
                Side side = gameController.getMySide();
                triggerEvent(gameController, side, GameMechanicEventType.END_TURN);

            }
    // Notify message event
    after(TextMessage message):execution(* TextMessagePublisher.notifyMessage( entity.event.TextMessage ))&& args(message)
            {
                triggerEvent(thisJoinPoint.getThis(), GameMechanicEventType.TEXT_MESSAGE, message);
            }
    // Game consistency changing events
    after(GameState state):execution(* controller.ControllerManager.setGameState( entity.GameState ))&& args(state)
            {
                triggerEvent(thisJoinPoint.getThis(), GameMechanicEventType.STATE_CHANGE, state);
                if (state.isGameOver()) triggerEvent(thisJoinPoint.getThis(), GameMechanicEventType.GAME_OVER, state);
            }
    //Particular events
    after(Side side):execution(* controller.ControllerManager.setCurrentSide( entity.Side ))&& args(side)
            {
                GameEvent gameEvent = new GameEvent(GameMechanicEventType.SIDE_CHANGE, side).setSourceSide(side);
                triggerEvent(thisJoinPoint.getThis(), gameEvent);
            }

    //Helper methods
    private void triggerEvent(Object source, GameMechanicEventType type, Object data) {
        GameEvent gameEvent = new GameEvent().setType(type).setData(data);
        triggerEvent(source, gameEvent);
    }

    private void triggerEvent(Object source, Side side, GameMechanicEventType type) {
        GameEvent gameEvent = new GameEvent(type).setSourceSide(side);
        triggerEvent(source, gameEvent);
    }

    private void triggerEvent(Object source, GameEvent gameEvent) {
        EventManager listener;
        if (source instanceof EventSpaceSupplement)
            listener = ((EventSpaceSupplement) source).getEventManager();
        else
            listener = EventManager.getEventListenerForMe(null, this);
        listener.triggerEvent(gameEvent);
    }
    declare parents:GameController implements EventSpaceSupplement;
    declare parents:TextMessagePublisher implements EventSpaceSupplement;
}