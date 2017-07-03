package server.controllers;

import JavaFxClient.controllers.JavaTavleiClientController;
import JavaFxClient.model.event.NetworkEventManager;
import entity.Move;
import entity.Side;
import entity.SideHasNoMovesException;
import gamemechanics.model.tavlei.TavleiBoard;
import gamemechanics.model.tavlei.TavleiBoardImpl;
import generated.ResponseType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import server.controllers.ServerStarter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Irina on 08.03.2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ServerStarter.class)
@WebIntegrationTest
public class TavleiWebSocketTest {

    private static final String WEBSOCKET_HOST = "http://localhost:1558";
    static MockJavaClient controller;
    static MockJavaClient availableOpponent;
    static Set<ResponseType> occurredEvents = new ConcurrentSkipListSet<>();

    @Before
    public void setup() {

        if (controller == null) {   //first invoke
            TavleiBoard tavleiBoard = new TavleiBoardImpl();
            controller = new MockJavaClient(WEBSOCKET_HOST, tavleiBoard);
            controller.connectToServer();
            availableOpponent = new MockJavaClient(WEBSOCKET_HOST, tavleiBoard);
            availableOpponent.connectToServer();
            for (ResponseType responseType : ResponseType.values()) {
                NetworkEventManager.getEventListener().addListener(responseType,
                        response -> occurredEvents.add(responseType), "testWebSocket");
            }

        }

    }

    @After
    public void cleanTest() {
        occurredEvents.clear();
        controller.stopGame();
        availableOpponent.stopGame();
    }

    @Test
    public void receiveAnyResponse() throws Exception {
        controller.startGame();
        for (int i = 0; i < 10; i++) {
            if (!occurredEvents.isEmpty()) return;
            else try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        fail();
    }

    @Test
    public void receivePartnerSearching() {
        controller.startGame();
        assertTrue(this.checkAnswer(5, ResponseType.PARTNER_SEARCHING));
    }

    private boolean checkAnswer(int timeSeconds, ResponseType... desirableResponseTypes) {
        for (int i = 0; i < timeSeconds * 2; i++) {
            for (ResponseType desirableResponseType : desirableResponseTypes) {
                if (occurredEvents.contains(desirableResponseType)) return true;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //do nothing!
            }
        }
        return false;
    }

    @Test
    public void gameInteraction() {
        //TODO: make sure that controller and availableOpponent play with each other and separate event space for those
        controller.startGame(Side.BLACK);
        availableOpponent.startGame(Side.WHITE);
        assertTrue(checkAnswer(5, ResponseType.PARTNER_FOUNDED));
        //if controller in the game...
        if (controller.getBehalfSide() != null) {
            controller.beginTurn();
            try {
                controller.randomBehalfMove();
            } catch (SideHasNoMovesException e) {
                return;//It's ok if side has not moves. But it's unlikely
            }
        }
        //Some one event must occur
        assertTrue("TURN_CONFIRMED or MAKE_TURN Answers Din't received", checkAnswer(7, ResponseType.TURN_CONFIRMED, ResponseType.MAKE_TURN));

    }

    @Test
    //test if websocket connection could be established
    public void connectible() throws InterruptedException, TimeoutException, ExecutionException {
        SimpleWebSocketClient simpleWebSocketClient = new SimpleWebSocketClient();
        simpleWebSocketClient.connectToServer(WEBSOCKET_HOST);
    }

    //Open access to Controller members and provide random move
    static private class MockJavaClient extends JavaTavleiClientController {
        public MockJavaClient(String host, TavleiBoard board) {
            super(host, board);
        }

        @Override
        public void connectToServer() {
            super.connectToServer();
        }

        @Override
        protected void onBehalfMovePropose(Move move) {
            super.onBehalfMovePropose(move);
        }

        @Override
        public TavleiBoard getBoard() {
            return super.getBoard();
        }

        public void randomBehalfMove() throws SideHasNoMovesException {
            onBehalfMovePropose(getBoard().generateAllMovesForSide(getBehalfSide()).values().stream().flatMap(Collection::stream).findAny().orElseThrow(() -> new SideHasNoMovesException("Behalf Side " + getBehalfSide() + " has no movies in the test")));
        }
    }

    static private class SimpleWebSocketClient {
        WebSocketStompClient stompClient;

        void connectToServer(String connectTo) throws ExecutionException, InterruptedException, TimeoutException {
            List<Transport> transports = new ArrayList<>(2);
            transports.add(new WebSocketTransport(new StandardWebSocketClient()));
            transports.add(new RestTemplateXhrTransport(new RestTemplate()));
            SockJsClient sockJsClient = new SockJsClient(transports);
            this.stompClient = new WebSocketStompClient(sockJsClient);
            ListenableFuture<StompSession> future =
                    this.stompClient.connect(connectTo + "/move", new StompSessionHandlerAdapter() {
                    });
            // <--- this line will wait just like afterConnected()
            future.get(5, SECONDS);
        }

    }
}


