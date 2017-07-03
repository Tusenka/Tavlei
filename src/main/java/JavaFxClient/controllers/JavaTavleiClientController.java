package JavaFxClient.controllers;

import JavaFxClient.model.event.NetworkEventManager;
import entity.*;
import entity.Logger.LoggableI;
import entity.event.GameEvent;
import entity.event.GameMechanicEventType;
import gamemechanics.controller.tavlei.TavleiControllerImpl;
import generated.*;
import gamemechanics.model.event.EventManager;
import gamemechanics.model.event.EventSpaceSupplement;
import gamemechanics.model.tavlei.TavleiBoard;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;


/**
 * This TavleiController allows you to play tavlei remotely by connection to the server.
 * Use spring Stomp/SockJS
 * <p>
 * Class must be overloadable. So it contain some protected methods.
 * Uses Singleton Pattern
 *
 * @author Irina
 */
public class JavaTavleiClientController extends TavleiControllerImpl implements EventSpaceSupplement, LoggableI {
    //There are specific variables, so I invoke them directly, omitting getters
    private WebSocketStompClient stompClient;
    private ProducerStompSessionHandler producer;
    private String serverAddress = "http://localhost:1558";
    private final String endPoint = "/move";

    private Side behalfSide; //getter and setter are exists
    private static volatile JavaTavleiClientController _instance;

    //TODO:: Add constructor with Side
    public static synchronized JavaTavleiClientController getClientController(String serverAddress, TavleiBoard board) {
        BiFunction<String, String, Boolean> isEmptyOrEqualTo = (s, s2) -> (s == null || s.isEmpty()) || (s.equals(s2));
        if (_instance == null) {
            _instance = new JavaTavleiClientController(serverAddress, board);
            _instance.connectToServer();
        }
        //If host changed
        else if (!isEmptyOrEqualTo.apply(serverAddress, _instance.serverAddress)) {
            _instance.serverAddress = serverAddress;
            _instance.reset();
        } else if (!_instance.isConnectible()) {
            _instance.reset();
        }
        _instance.setBoard(board);
        return _instance;
    }

    /**
     * Constructor for connecting to a hosted name
     *
     * @param host  Server host address with http of the other player. Without port.
     * @param board
     */
    protected JavaTavleiClientController(String host, TavleiBoard board) {
        super(Side.BLACK, board);
        setAddress(host);
        createStompClient();
        subscribe();
    }

    @SuppressWarnings("WeakerAccess")
    protected void reset() {
        createStompClient();
        reSubscribe();
        connectToServer();
    }

    @SuppressWarnings("WeakerAccess")
    protected void setAddress(String connectTo) {
        if (connectTo != null && !connectTo.isEmpty()) {
            this.serverAddress = connectTo;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public Side getBehalfSide() {
        return behalfSide;
    }

    @SuppressWarnings("WeakerAccess")
    public void setBehalfSide(Side behalfSide) {
        this.behalfSide = behalfSide;
    }

    @SuppressWarnings("WeakerAccess")
    protected void reSubscribe() {
        this.unsubscribe();
        subscribe();
    }

    @SuppressWarnings("WeakerAccess")
    protected void subscribe() {
        this.unsubscribe();
        EventManager eventManager = getEventManager();
        eventManager.addListener(GameMechanicEventType.MOVE,
                gameEvent -> {
                    try {
                        //If the behalf player moved, send the move to the server;
                        if (this.getBehalfSide() != null && this.getBehalfSide().equals(gameEvent.getSourceSide())) {
                            onBehalfMovePropose((Move) gameEvent.getData());
                        }
                    } catch (GameRulesException e) {
                        eventManager.triggerEvent(new GameEvent(
                                GameMechanicEventType.GAME_RULES_ERROR, e.toString()));
                    }
                });
    }

    @SuppressWarnings("WeakerAccess")
    protected void onBehalfMovePropose(Move move) {
        producer.sendMove(move);
    }

    @SuppressWarnings("WeakerAccess")
    protected boolean isConnectible() {
        return (stompClient == null || producer == null || !producer.isClosed());
    }


    @Override
    public void clear() {
        //Due it is a singleton do nothing on clear
    }

    @SuppressWarnings("WeakerAccess")
    protected void createStompClient() {
        List<Transport> transports = new ArrayList<>(2);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        RestTemplateXhrTransport xhrTransport = new RestTemplateXhrTransport(new RestTemplate());
        transports.add(xhrTransport);
        SockJsClient sockJsClient = new SockJsClient(transports);
        if (this.stompClient != null) this.stompClient.stop();
        this.stompClient = new WebSocketStompClient(sockJsClient);
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.afterPropertiesSet();
        stompClient.setTaskScheduler(taskScheduler);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        producer = new ProducerStompSessionHandler();
    }

    @SuppressWarnings("WeakerAccess")
    protected void connectToServer() {
        ListenableFuture<StompSession> future =
                this.stompClient.connect(this.serverAddress + endPoint, producer);
        try {
            // <--- this line will wait just like afterConnected()
            future.get();

        } catch (ExecutionException | InterruptedException e) {
            getLog().warn("Session was interrupted: " + e);
        }
    }

    @Override
    public void startGame() {
        //stopGame();
        producer.startGame();
    }

    public void startGame(Side desirableSide) {
        producer.startGame(desirableSide);
    }

    @Override
    public void stopGame() {
        this.behalfSide = null;
        producer.stopGame();
    }

    @Override
    public void makeMove(Move possibleMove) throws IllegalMoveException {
        super.getBoard().addInfoMove(possibleMove, getBoard().getSideFromPosition(possibleMove.getStart()));
        super.makeMove(possibleMove);
    }

    public static class NotConnectedException extends RuntimeException {
        NotConnectedException(String message) {
            super(message);
            GameMechanicResponse response = new GameMechanicResponse();
            response.setType(ResponseType.INTERRUPT_GAME);
            response.setMessage("The game is interrupted. Reason: " + message);
            NetworkEventManager.getEventListener().triggerEvent(response);
        }

        @SuppressWarnings("SameParameterValue")
        NotConnectedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    protected class ProducerStompSessionHandler extends StompSessionHandlerAdapter {
        private String webSocketSessionId;
        private StompSession session;
        private final java.util.concurrent.Semaphore isSending = new java.util.concurrent.Semaphore(1, true);
        private boolean isRunning = false;
        private boolean isClosed = false;

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            super.afterConnected(session, connectedHeaders);
            this.session = session;
            session.setAutoReceipt(true);
            try {
                isSending.tryAcquire(10, TimeUnit.SECONDS);
                receiveWebSocketSessionId(session, webSocketSessionId -> {
                    session.subscribe("/result/move/" + webSocketSessionId, this);
                    this.webSocketSessionId = webSocketSessionId;
                    isSending.release();
                    getLog().debug("WebSocket session is received:" + this.webSocketSessionId);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new NotConnectedException("Can't get Session information via webSocket. Someone else is trying to send by this chanel", e);
            }

        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return GameMechanicResponse.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            GameMechanicResponse response = (GameMechanicResponse) payload;
            switch (response.getType()) {
                case MAKE_TURN: {
                    getOuter().makeMove(response.getMove());
                }
                break;
                case WIN_GAME:
                    getOuter().checkGameState();
                    break;
                case INTERRUPT_GAME:
                    stopGame();
                    getLog().debug("Game was interrupted by other player.");
                    response.setMessage("The game was interrupted by other player.");
                    NetworkEventManager.getEventListener().triggerEvent(response);
                    break;
                case TURN_CONFIRMED://do nothing
                    break;
                case TURN_UNCONFIRMED:
                    stopGame();
                    break;
                case PARTNER_SEARCHING:
                    getOuter().setCurrentState(TavleiState.HOSTING);
                    break;
                case PARTNER_FOUNDED://do nothing
                    getOuter().setCurrentState(TavleiState.ONGOING);
                    getOuter().setBehalfSide(response.getYourSide());
                    getLog().debug("Network multilayer game begin.");
                    break;
                case PARTNER_NOT_FOUNDED:
                    stopGame();
                    break;
            }
            NetworkEventManager.getEventListener().triggerEvent(response);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            super.handleTransportError(session, exception);
            isClosed = true;
            GameMechanicResponse response = new GameMechanicResponse();
            response.setType(ResponseType.INTERRUPT_GAME);
            response.setMessage("Lost connection to the server.");
            NetworkEventManager.getEventListener().triggerEvent(response);
        }

        boolean isClosed() {
            return isClosed;
        }

        JavaTavleiClientController getOuter() {
            return JavaTavleiClientController.this;

        }

        public void startGame() {
            startGame(null);
        }

        public void startGame(Side desirableSide) {
            isRunning = true; //Must be at first line, before sending request.
            GameMechanicRequest request = new GameMechanicRequest();
            request.setType(RequestType.GAME_BEGIN);
            request.setMode(GameModeType.MULTILAYER);
            request.setMySide(desirableSide);
            getLog().debug("Trying to start multiplayer game..." + getOuter());
            sendGameRequest(request);
            //TODO::Aspect logger getLog().debug("Trying to start multiplayer game...");
        }

        void stopGame() {
            isRunning = false;
            GameMechanicRequest request = new GameMechanicRequest();
            request.setType(RequestType.GAME_END);
            sendGameRequest(request);
        }

        void sendMove(Move move) {
            GameMechanicRequest request = new GameMechanicRequest();
            request.setType(RequestType.MAKE_TURN);
            request.setMove(move);
            sendGameRequest(request);
        }

        void sendGameRequest(GameMechanicRequest request) {
            //If game stopped do nothing
            if (!isRunning) return;

            if (this.session == null) {
                throw new NotConnectedException("Client can't send " + request.getType() + " to " + getOuter().serverAddress + getOuter().endPoint + ". Client doesn't connected to the server.");
            }

            StompHeaders stompHeaders = new StompHeaders();
            stompHeaders.setDestination("/app" + getOuter().endPoint);
            stompHeaders.setContentType(MimeTypeUtils.APPLICATION_JSON);
            try {
                this.isSending.tryAcquire(10, TimeUnit.SECONDS);
                this.session.send(stompHeaders, request);
                getLog().debug("Sending request:" + request.getType() + request.toString() + " from " + getOuter());
                this.isSending.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
                session.disconnect();
                throw new NotConnectedException("Can't send message to the server via WebSocket. Someone else trying to send by this chanel");
            }
        }

        private void receiveWebSocketSessionId(StompSession session, Consumer<String> afterReceiveFunc) {
            session.subscribe("/result" + getOuter().endPoint + "/yourSessionId/" + session.getSessionId(), new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return SessionResponse.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    webSocketSessionId = ((SessionResponse) payload).getYourSessionId();
                    afterReceiveFunc.accept(webSocketSessionId);
                }
            });
            //Get to know session Id
            SessionRequest sessionRequest = new SessionRequest();
            sessionRequest.setSendTo(session.getSessionId());
            session.send("/app/help/" + getOuter().endPoint, sessionRequest);
        }

    }


}
