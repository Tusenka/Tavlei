package gamemechanics.controller.tavlei;

import gamemechanics.controller.ControllerManager;
import entity.GameRulesException;
import entity.GameState;
import entity.Side;
import entity.TavleiState;
import entity.event.GameEvent;
import entity.event.GameMechanicEventType;
import gamemechanics.controller.ai.tavlei.AITavleiController;
import gamemechanics.model.event.EventManager;
import gamemechanics.model.event.EventSpaceSupplement;
import gamemechanics.model.tavlei.TavleiBoard;
import gamemechanics.model.tavlei.TavleiBoardImpl;


import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Created by Irina
 * Manage interaction and check turn order among game controllers. It's the highest layer for interaction "outer world" with game mechanic layer .
 * Interact by @{@link GameEvent} events.
 * Uses pattern
 */
public class TavleiControllerManager implements ControllerManager, EventSpaceSupplement {
    protected final Map<Side, TavleiController> controllers = new EnumMap<>(Side.class);
    private Side currentSide = Side.WHITE;
    private TavleiBoard board;
    private GameState gameState = TavleiState.ONGOING;

    private static ExecutorService controllerThread;
    private static final ThreadGroup controllerThreadGroup = new ThreadGroup("server.controllers eval");

    public TavleiControllerManager() {
        this(null);
    }

    public TavleiControllerManager(String newEventSpace) {
        this.baseSetEventSpace(newEventSpace);
        createControllerThread();
        subscribe();
    }

    @SuppressWarnings("WeakerAccess")
    protected void subscribe() {
        EventManager eventManager = getEventManager();
        eventManager.addListener(GameMechanicEventType.MOVE, event -> {
            if (getCurrentSide().equals(event.getSourceSide()))
                endTurn();
            else
                getEventManager().triggerEvent(
                        new GameEvent(GameMechanicEventType.GAME_RULES_ERROR, "Side " + event.getSourceSide() + " can't perform move." + "It's " + getCurrentSide() + "'s turn!")
                );
        });
    }

    /**
     * Set new event space and subscribe it.
     *
     * @param eventSpaceName New event space name
     */
    public TavleiControllerManager setEventSpace(String eventSpaceName) {
        unsubscribe();
        this.baseSetEventSpace(eventSpaceName);
        setChildEventSpace();
        subscribe();
        return this;
    }

    @SuppressWarnings("WeakerAccess")
    protected void setChildEventSpace() {
        this.controllers.forEach((side, tavleiController) ->
                {
                    if (tavleiController instanceof EventSpaceSupplement)
                        ((EventSpaceSupplement) tavleiController).setEventSpace(getEventSpace());
                }
        );
    }

    @Override
    public void reset() {
        stopGame();
        board = new TavleiBoardImpl();
    }

    @Override
    public void playWithLocalUser() {
        reset();
        this.controllers.put(Side.WHITE, new LocalUserTavleiController(Side.WHITE, board));
        this.controllers.put(Side.BLACK, new LocalUserTavleiController(Side.BLACK, board));
        setChildEventSpace();
        setCurrentSide(Side.BLACK);
    }

    @Override
    public void startGame() {
        setGameState(TavleiState.ONGOING);
        beginTurn();
    }

    @Override
    public void stopGame() {
        interruptControllerThread();
        controllers.forEach((side, tavleiController) -> tavleiController.clear());
        controllers.clear();
    }

    @Override
    public void playWithCompute(Side humanSide) {
        reset();
        board = new TavleiBoardImpl();
        controllers.put(humanSide, new LocalUserTavleiController(humanSide, board));
        controllers.put(humanSide.getOppositeSide(), new AITavleiController(humanSide.getOppositeSide(), board));
        setChildEventSpace();
        setCurrentSide(Side.BLACK);
    }

    public void setCurrentSide(Side s) {
        currentSide = s;
    }

    @Override
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public void joinToServer(String address) {
        //do nothing
    }

    @Override
    public void beginTurn() {
        controllerThread.submit(() -> {
            try {
                getCurrentController().beginTurn();
            } catch (GameRulesException e) {
                getEventManager().triggerEvent(
                        new GameEvent(GameMechanicEventType.GAME_RULES_ERROR, e.toString())
                );
            }
        });
    }

    @Override
    public TavleiController getCurrentController() {
        return controllers.get(currentSide);
    }

    @Override
    public void endTurn() {
        controllers.forEach((side, tavleiController) -> tavleiController.endTurn());
        setGameState(getCurrentController().getCurrentState());
        setCurrentSide(currentSide.getOppositeSide());
        if (!gameState.isGameOver()) beginTurn();
    }

    @Override
    public Side getCurrentSide() {
        return currentSide;
    }

    @Override
    public TavleiBoard getBoard() {
        return board;
    }


    @Override
    public GameState getCurrentState() {

        return gameState;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod") //It mustn't call parent clone
    @Override
    public ControllerManager clone() {
        return new TavleiControllerManager(this.getEventSpace());
    }

    @Override
    public ControllerManager clone(String newEventSpace) {
        return new TavleiControllerManager(newEventSpace);
    }

    @Override
    public void clear() {
        unsubscribe();
        interruptControllerThread();
        controllers.forEach((side, tavleiController) -> tavleiController.clear());
        controllers.clear();
    }

    private void interruptControllerThread() {
        if (controllerThread == null) return;
        controllerThread.shutdownNow();
        try {
            controllerThread.awaitTermination(10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            //do nothing
        }
        createControllerThread();
    }

    protected void createControllerThread() {
        controllerThread = Executors.newSingleThreadExecutor(r -> new Thread(controllerThreadGroup, r));
    }

    public static ExecutorService getControllerThread() {
        return controllerThread;
    }
}
