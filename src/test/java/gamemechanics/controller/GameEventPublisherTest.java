package gamemechanics.controller;

import gamemechanics.controller.ControllerManager;
import entity.Move;
import entity.Position;
import entity.Side;
import entity.event.GameEvent;
import entity.event.GameMechanicEventType;
import gamemechanics.controller.tavlei.TavleiControllerManager;
import gamemechanics.model.event.EventManager;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

/**
 * Created by Irina on 08.03.2017.
 */
public class GameEventPublisherTest {
    static ControllerManager controllerManager;
    static String eventSpace = "testEvents";
    static EventManager spyEventManager;

    @BeforeClass
    public static void setUp() {
        try {
            Method getEventListener = EventManager.class.getDeclaredMethod("getEventListener", String.class);
            getEventListener.setAccessible(true);
            spyEventManager = Mockito.spy((EventManager) getEventListener.invoke(null, eventSpace));
            getEventListener.setAccessible(false);
            Method putEventListeners = EventManager.class.getDeclaredMethod("putEventListener", String.class, EventManager.class);
            putEventListeners.setAccessible(true);
            putEventListeners.invoke(null, eventSpace, spyEventManager);
            putEventListeners.setAccessible(false);
            Mockito.reset(spyEventManager);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        controllerManager = new TavleiControllerManager(eventSpace);


    }

    @After
    public void cleanTest() {
        controllerManager.reset();
        Mockito.reset(spyEventManager);
    }

    @Test
    public void onBeginTurn() throws InterruptedException {
        startGame();
        checkEvent(GameMechanicEventType.BEGIN_TURN);
    }

    @Test
    public void onEndTurn() throws InterruptedException {
        startGame();
        makeHumanTurn();
        checkEvent(GameMechanicEventType.END_TURN);
    }

    @Test
    public void onMove() throws InterruptedException {
        startGame();
        makeHumanTurn();
        checkEvent(GameMechanicEventType.MOVE);
    }

    private void startGame() throws InterruptedException {
        controllerManager.playWithLocalUser();
        controllerManager.startGame();
        //Waiting for controllers setup.. This is quite enough. Alternative is to catch events... But I don't won't to do that.
        TimeUnit.MILLISECONDS.sleep(20);

    }

    private void makeHumanTurn() throws InterruptedException {
        Move move = new Move(new Position(4, 7), new Position(2, 7));
        spyEventManager.triggerEvent(new GameEvent(GameMechanicEventType.PROPOSE_MOVE, move).setSourceSide(Side.BLACK));
        //Waiting for controllers setup. This quite enough. Alternative is to catch events... But I don't won't to do that.
        TimeUnit.MILLISECONDS.sleep(20);
    }

    private void checkEvent(GameMechanicEventType eventType) {
        verify(spyEventManager, atLeastOnce()).triggerEvent(argThat(new BaseMatcher<GameEvent>() {
                                                                        public void describeTo(Description description) {
                                                                        }

                                                                        @Override
                                                                        public boolean matches(Object item) {
                                                                            return eventType.toString().equals(((GameEvent) item).getType());
                                                                        }
                                                                    }
        ));
    }
}