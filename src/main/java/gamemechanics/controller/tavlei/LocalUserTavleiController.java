package gamemechanics.controller.tavlei;

import entity.GameRulesException;
import entity.Move;
import entity.Side;
import entity.event.GameEvent;
import entity.event.GameMechanicEventType;
import gamemechanics.model.event.EventSpaceSupplement;
import gamemechanics.model.tavlei.TavleiBoard;


import java.lang.ref.WeakReference;

/**
 * Local user controller behalves a human player locally. It doesn't use Network.
 * It interacts with human by GameMechanicEvents. Listen event @{@link GameMechanicEventType}.PROPOSE_MOVE and makes turn.
 */
public class LocalUserTavleiController extends TavleiControllerImpl implements EventSpaceSupplement {
    public LocalUserTavleiController() {
        subscribe();
    }

    public LocalUserTavleiController(Side s, TavleiBoard board) {
        super(s, board);
        subscribe();
    }

    @Override
    public EventSpaceSupplement setEventSpace(String eventSpaceName) {
        this.unsubscribe();
        this.baseSetEventSpace(eventSpaceName);
        subscribe();
        return this;
    }

    @SuppressWarnings("WeakerAccess") //Subscribe must be overridable for child classes
    protected void subscribe() {
        WeakReference<LocalUserTavleiController> weakThis = new WeakReference<>(this);
        getEventManager().addListener(GameMechanicEventType.PROPOSE_MOVE, gameEvent -> {
            LocalUserTavleiController localThis = weakThis.get();
            if (localThis == null) return;
            try {
                if (localThis.getMySide().equals(gameEvent.getSourceSide())) {
                    localThis.onMovePropose((Move) gameEvent.getData());
                }
            } catch (GameRulesException e) {
                localThis.getEventManager().triggerEvent(new GameEvent(
                        GameMechanicEventType.GAME_RULES_ERROR, e.toString()));
            }
        });
    }

    @SuppressWarnings("WeakerAccess")//Reaction to human user move must be overridable and available
    protected void onMovePropose(Move move) {
        makeMove(move);
    }

    @Override
    public void clear() {
        unsubscribe();
    }
}
