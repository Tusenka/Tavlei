package gamemechanics.controller.tavlei;

import entity.Move;
import entity.Position;
import entity.Side;
import entity.event.GameEvent;
import entity.event.GameMechanicEventType;
import gamemechanics.model.event.EventManager;
import gamemechanics.model.tavlei.TavleiBoard;
import gamemechanics.model.tavlei.TavleiBoardImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import server.controllers.ServerStarter;
//import server.controllers.ServerStarter;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

/**
 * Created by Irina on 08.03.2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ServerStarter.class)
@WebIntegrationTest
public class LocalUserTavleiControllerTest {

    @Rule
    public Timeout timeout = Timeout.millis(300);
    //Spy
    private LocalUserTavleiController tavleiController;
    private String mockEventSpace = "testEventSpace";
    private EventManager eventManger;

    @Before
    public void setUp() throws Exception {
        tavleiController = Mockito.spy(new LocalUserTavleiController());
        mockEventSpace = mockEventSpace + System.currentTimeMillis();
        tavleiController.setEventSpace(mockEventSpace);
        Mockito.reset(tavleiController);
        TavleiBoardImpl tavleiBoard = new TavleiBoardImpl();
        //set board in the controllerManager
        Method setBoard = ((TavleiControllerImpl) tavleiController).getClass().getDeclaredMethod("setBoard", TavleiBoard.class);
        setBoard.setAccessible(true);
        setBoard.invoke(tavleiController, tavleiBoard);
        setBoard.setAccessible(false);
        tavleiController.setMySide(Side.BLACK);
        eventManger = EventManager.getEventListenerForMe(mockEventSpace, this);
        Mockito.reset(tavleiController);

    }

    @After
    public void clearAll() throws Exception {
        tavleiController.clear();
        Mockito.reset(tavleiController);
        eventManger.unsubscribeMe();
    }

    @Test
    public void setEventSpace() throws Exception {
        tavleiController.setEventSpace("false");
        Move move = new Move(new Position(3, 0), new Position(1, 0));
        eventManger.triggerEvent(new GameEvent(GameMechanicEventType.PROPOSE_MOVE, move).setSourceSide(Side.BLACK));
        Mockito.verify(this.tavleiController, Mockito.never()).onMovePropose(Mockito.anyObject());
    }

    @Test
    public void subscribe() throws Exception {
        onMovePropose();
    }

    @Test
    public void onMovePropose() throws Exception {
        Move move = new Move(new Position(3, 0), new Position(1, 0));
        eventManger.triggerEvent(new GameEvent(GameMechanicEventType.PROPOSE_MOVE, move).setSourceSide(Side.BLACK));
        Mockito.verify(this.tavleiController, times(1)).onMovePropose(move);
    }

    @Test
    public void clear() throws Exception {
        tavleiController.clear();
        Move move = new Move(new Position(3, 0), new Position(1, 0));
        eventManger.triggerEvent(new GameEvent(GameMechanicEventType.PROPOSE_MOVE, move).setSourceSide(Side.BLACK));
        Mockito.verify(this.tavleiController, Mockito.never()).onMovePropose(move);
    }

    @Test
    public void getEventSpace() throws Exception {
        assertEquals(mockEventSpace, tavleiController.getEventSpace());
    }

    @Test
    public void baseSetEventSpace() throws Exception {
        //As a part of AspectJ implementation, it mustn't be tested here.
    }

    @Test(timeout = 300) //TODO: why this simple test longer than 200
    public void getEventManager() throws Exception {
        EventManager testManager = tavleiController.getEventManager();
        assertEquals(testManager.getEventSpace(), mockEventSpace);
        assertTrue(testManager instanceof EventManager.EventManagerMock);

    }

    @Test
    public void unsubscribe() throws Exception {
        tavleiController.unsubscribe();
        Move move = new Move(new Position(3, 0), new Position(1, 0));
        eventManger.triggerEvent(new GameEvent(GameMechanicEventType.PROPOSE_MOVE, move).setSourceSide(Side.BLACK));
        Mockito.verify(this.tavleiController, Mockito.never()).onMovePropose(move);
    }

}