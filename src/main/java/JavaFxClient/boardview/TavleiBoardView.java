package JavaFxClient.boardview;

import JavaFxClient.model.MyAppDataConfig;
import JavaFxClient.model.event.NetworkEventManager;
import entity.*;
import entity.event.GameEvent;
import entity.event.GameMechanicEventType;
import entity.event.TextMessage;
import gamemechanics.controller.tavlei.LocalUserTavleiController;
import gamemechanics.controller.tavlei.TavleiController;
import gamemechanics.controller.tavlei.TavleiControllerManager;
import gamemechanics.model.Piece;
import gamemechanics.model.event.EventManager;
import gamemechanics.model.tavlei.TavleiBoard;
import gamemechanics.model.tavlei.TavleiPiece;
import generated.ResponseType;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class for a view for a tavlei board.
 *
 * @author Irina
 * @date Dec 24, 2016
 */
@SuppressWarnings("WeakerAccess")
public class TavleiBoardView implements BoardView {
    @SuppressWarnings("CanBeFinal")//Theoretically could be dynamically replaced
    protected TavleiControllerManager controllerManager;

    @Override
    public byte getBoardSize() {
        return boardSize;
    }

    private final byte boardSize;
    private final GridPane gridPane;
    private final Tile[][] tiles;
    // private final Text sideStatus;
    private Side mySide = Side.BLACK;

    @Override
    public Label getTurnLabel() {
        return turnLabel;
    }

    @Override
    public void setTurnLabel(Label turnLabel) {
        this.turnLabel = turnLabel;
    }

    @Override
    public Label getStateLabel() {
        return stateLabel;
    }

    @Override
    public void setStateLabel(Label stateLabel) {
        this.stateLabel = stateLabel;
    }

    private void setStateLabelText(String text) {
        Platform.runLater(() -> this.stateLabel.setText(text));
    }

    Label turnLabel;
    Label stateLabel;
    private final MyAppConfigurationLoader config;
    private Move adviseMove;
    private static ExecutorService adviseThread;
    private static final ThreadGroup controllerThreadGroup = new ThreadGroup("adviceTurn eval");

    private final AtomicBoolean blocked = new AtomicBoolean(false);

    /* Instance variable needed to decide if click on tile was first or second click */
    private Position selectedPiece;

    /**
     * Construct a BoardView with an instance of a GameController
     * and a couple of Text object for displaying info to the user
     */
    TavleiBoardView() {
        ApplicationContext context = MyAppDataConfig.getContext();
        this.controllerManager = (TavleiControllerManager) context.getBean("defaultTavleiControllerManager");
        if (adviseThread == null) createControllerThread();
        //this.sideStatus = sideStatus;
        this.boardSize = controllerManager.getBoard().getSize();
        tiles = new Tile[this.boardSize][this.boardSize];
        this.config = loadBuildConfiguration();
        gridPane = new GridPane();
        gridPane.setStyle(config.getString("grid-pane.style"));
        reset();
        setupListener();
    }

    private void createControllerThread() {
        adviseThread = Executors.newSingleThreadExecutor(r -> new Thread(controllerThreadGroup, r));
    }

    protected TavleiBoard getBoardModel() {
        return controllerManager.getBoard();
    }

    private TavleiController getCurrentController() {
        return controllerManager.getCurrentController();
    }

    public void interruptAdviseThread() {
        if (adviseThread == null) return;
        adviseThread.shutdownNow();
        try {
            adviseThread.awaitTermination(10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        unlockPasses();
        createControllerThread();
    }

    private void blockPasses() {
        this.blocked.set(true);
    }

    private void unlockPasses() {
        this.blocked.set(false);

    }

    private MyAppConfigurationLoader loadBuildConfiguration() {
        MyAppConfigurationLoader config;
        try {
            config = new MyAppConfigurationLoader("TavleiBoardView.properties");
        } catch (MyAppConfigurationLoader.PropertiesException e) {
            e.printStackTrace();
            return new MyAppConfigurationLoader();
        }
        return config;

    }

    private void beginTurn(Side newSide) {
        //If current server.controllers for local user, switch side
        if ((getCurrentController() instanceof LocalUserTavleiController))
            setMySide(getCurrentController().getMySide());
        if (!getMySide().equals(newSide)) blockPasses();
        if (getMySide().equals(newSide)) unlockPasses();
    }

    public void endTurn() {
        unlockPasses();
    }


    /**
     * Listener for clicks on a tile
     *
     * @param tile The tile attached to this listener
     * @return The event handler for all tiles.
     */
    private EventHandler<? super MouseEvent> tileListener(Tile tile) {
        return event -> {
            // Don't change the code above this :)
            //Check whose piece it is
            if (getCurrentController() == null) return;
            Set<Move> moves = getCurrentController().getMovesForPieceAt(tile.getPosition());
            if (selectedPiece == null && !moves.isEmpty()) {
                selectedPiece = tile.getPosition();
                firstClick(tile);

            } else {
                if (selectedPiece != null) {
                    secondClick(tile);
                }
            }
        };
    }


    /**
     * Perform the first click functions, like displaying
     * which are the valid moves for the piece you clicked.
     *
     * @param tile The TileView that was clicked
     */
    private void firstClick(Tile tile) {
        if (this.blocked.get()) {
            return;
        }
        Set<Move> moves = controllerManager.getCurrentController().getMovesForPieceAt(selectedPiece);
        if ((tile.getPosition().getRow() + tile.getPosition().getCol()) % 2 == 0) {
            tile.highlight(config.getFXColor("selected.odd.color"));
        } else {
            tile.highlight(config.getFXColor("selected.even.color"));
        }

        for (Move currentMove : moves) {
            Position destination = currentMove.getDestination();
            if ((destination.getRow() + destination.getCol()) % 2 == 0) {
                tiles[destination.getRow()][destination.getCol()].highlight(config.getFXColor("movable.odd.color"));
            } else {
                tiles[destination.getRow()][destination.getCol()].highlight(config.getFXColor("movable.even.color"));
            }
            if (!currentMove.getDefeated().isEmpty()) {
                tiles[destination.getRow()][destination.getCol()].highlight(config.getFXColor("attack.color"));
                for (Position defPosition : currentMove.getDefeated()) {
                    tiles[defPosition.getRow()][defPosition.getCol()].highlight(config.getFXColor("defeated.color"));
                }
            }
        }
    }

    protected void adviseMove(Move move) {
        adviseMove = move;
        Position start = move.getStart();
        Position destination = move.getDestination();
        tiles[start.getRow()][start.getCol()].highlight(config.getFXColor("advise.start.color"));
        tiles[destination.getRow()][destination.getCol()].highlight(config.getFXColor("advise.destination"));
    }

    /**
     * Perform the second click functions, like
     * sending moves to the controllerManager but also
     * checking that the user clicked on a valid position.
     * If they click on the same piece they clicked on for the first click
     * then you should reset to click state back to the first click and clear
     * the highlighting effected on the board.
     *
     * @param tile the TileView at which the second click occurred
     */
    private void secondClick(Tile tile) {
        if (this.blocked.get()) {
            return;
        }
        //Clear allowed moves
        Set<Move> moves = getCurrentController().getMovesForPieceAt(selectedPiece);
        for (Move currentMove : moves) {
            Position destination = currentMove.getDestination();
            tiles[destination.getRow()][destination.getCol()].clear();
        }
        if (adviseMove != null) {
            Position start = adviseMove.getStart();
            Position destination = adviseMove.getDestination();
            getTileAt(start).clear();
            getTileAt(destination).clear();

        }

        getTileAt(selectedPiece).clear();

        Position target = tile.getPosition();
        //If valid move
        Move currentMove = new Move(selectedPiece, target);
        if (getCurrentController().getMovesForPieceAt(selectedPiece).contains(currentMove)) {
            try {
                tiles[target.getRow()][target.getCol()].setSymbol("");
                makeMove(currentMove);
            } catch (IllegalSideTurn e) {
                handleMessage(new TextMessage("Turn order error").setBody("Someone is trying to cheat." + e));
            } catch (IllegalMoveException e) {
                handleMessage(new TextMessage("Move exception").setBody("Someone is trying to cheat." + e));
            }
        }

        selectedPiece = null;

    }

    protected void makeMove(Move myMove) {
        EventManager eventManager = EventManager.getDefaultEventListener();
        //can only trigger PROPOSE_MOVE. Not GameMechanicEventType.MOVE! GameMechanicEventType.MOVE are for checked moves;
        eventManager.triggerEvent(new GameEvent(GameMechanicEventType.PROPOSE_MOVE, myMove).setSourceSide(getMySide()));
    }

    /**
     * This method should be called any time a move is made on the back end.
     * It should update the tiles' highlighting and symbols to reflect the
     * change in the board state.
     *
     * @param moveMade the move to show on the view
     */
    public void updateView(Move moveMade) {
        Position start = moveMade.getStart();
        Position destination = moveMade.getDestination();
        getTileAt(start).setSymbol("");
        getTileAt(destination).setSymbol("");
        getTileAt(destination).setSymbol(getSymbolForPieceAt(destination));
        //remover defeated pieces
        for (Position pToRemove : moveMade.getDefeated()) {
            tiles[pToRemove.getRow()][pToRemove.getCol()].setSymbol("");
        }
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                tiles[i][j].clear();
            }
        }
        tiles[destination.getRow()][destination.getCol()].highlight(config.getFXColor("last.destination.color"));
        tiles[start.getRow()][start.getCol()].highlight(config.getFXColor("last.start.color"));

    }

    protected String getSymbolForPieceAt(Position position) {
        TavleiPiece piece = (TavleiPiece) controllerManager.getBoard().getPieceAt(position);
        if (piece == null) return "";
        return TavleiPieceSymbols.getSymbol(piece);
    }

    public void setupListener() {
        EventManager eventManager = EventManager.getDefaultEventListener();

        eventManager.addListener(GameMechanicEventType.MOVE, e -> handleMove((Move) e.getData()));
        eventManager.addListener(GameMechanicEventType.SIDE_CHANGE, e -> handleSideChange((Side) e.getData()));
        eventManager.addListener(GameMechanicEventType.STATE_CHANGE, e -> handleGameStateChange((GameState) e.getData()));
        //if another side perform turn wait
        eventManager.addListener(GameMechanicEventType.END_TURN, e -> endTurn());
        eventManager.addListener(GameMechanicEventType.BEGIN_TURN, e -> beginTurn(e.getSourceSide()));

        eventManager.addListener(GameMechanicEventType.TEXT_MESSAGE, e -> handleMessage((TextMessage) e.getData()));

        eventManager.addListener(GameMechanicEventType.GAME_RULES_ERROR,
                e -> handleMessage(new TextMessage("Game rule error.").setBody(e.getData().toString()))
        );
        //subscribe to JavaClientEvents
        NetworkEventManager networkEventManager = NetworkEventManager.getEventListener();

        networkEventManager.addListener(ResponseType.PARTNER_SEARCHING, response -> {
            setStateLabelText("Partner searching...");
            blockPasses();
        }, getClass().getName());


        networkEventManager.addListener(ResponseType.PARTNER_FOUNDED, response -> {
            setMySide(response.getYourSide());
            setStateLabelText(TavleiState.ONGOING.toString());
            handleMessage(new TextMessage("Game begin! Your side: " + response.getYourSide()));
            unlockPasses();
        }, getClass().getName());

        networkEventManager.addListener(ResponseType.INTERRUPT_GAME, response -> {
            if (response.getMessage().isEmpty()) response.setMessage("Server or another player interrupt the game.");
            handleMessage(new TextMessage("Game was interrupted!").setBody(response.getMessage()));
            reset();
        }, getClass().getName());

        networkEventManager.addListener(ResponseType.TURN_UNCONFIRMED, response -> {
            handleMessage(new TextMessage("Server hadn't approve last move.").setBody("The game will be interrupted. " + response.getMessage()));
            reset();
        }, getClass().getName());


    }


    /**
     * Handles a change in the GameState (ie someone in check or stalemate).
     * If the game is over, it should open an Alert and ask to keep
     * playing or exit.
     *
     * @param s The new Game State
     */
    private void handleGameStateChange(GameState s) {
        setStateLabelText(s.toString());
        if (s.isGameOver()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle(s.toString());
                alert.setHeaderText(null);
                alert.setContentText("The game ended with: " + s.toString());
                alert.showAndWait();
            });
        }

    }


    /**
     * Handles messages from controllerManager (ie someone in check or stalemate).
     *
     * @param m The message
     */
    public void handleMessage(TextMessage m) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(m.getTitle());
            alert.setHeaderText(m.getHeader());
            alert.setContentText(m.getBody());
            alert.showAndWait();
        });
    }

    /**
     * Handles messages from controllerManager (ie someone in check or stalemate).
     *
     * @param m The Move
     */
    public void handleMove(Move m) {
        Platform.runLater(
                () -> updateView(m));
    }

    /**
     * Updates UI that depends upon which Side's turn it is
     *
     * @param s New side
     */
    private void handleSideChange(Side s) {
        Platform.runLater(
                () -> turnLabel.setText(s.toString() + "'s Turn"));
    }

    /**
     * Resets this BoardView with a new controllerManager.
     * This moves the chess pieces back to their original configuration
     */
    public void reset() {
        selectedPiece = null;
        interruptAdviseThread();
        addPieces();
        //Set special colors for tiles
        getBoardModel().getExits().forEach(p -> tiles[p.getRow()][p.getCol()].setColor(config.getFXColor("exits.color")));
        Position p = getBoardModel().getThrone();
        tiles[p.getRow()][p.getCol()].setColor(config.getFXColor("throne.color"));

    }

    /**
     * Initializes the gridPane object with the pieces from the GameController.
     * This method should only be called once before starting the game.
     */
    private void addPieces() {
        gridPane.getChildren().clear();
        Map<Piece, Position> pieces = getBoardModel().getAllActivePiecesPositions();
        /* Add the tiles */
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Tile tile = new TileView(new Position(row, col));
                gridPane.add(tile.getRootNode(),
                        1 + tile.getPosition().getCol(),
                        1 + tile.getPosition().getRow());
                GridPane.setHgrow(tile.getRootNode(), Priority.ALWAYS);
                GridPane.setVgrow(tile.getRootNode(), Priority.ALWAYS);
                getTiles()[row][col] = tile;
                tile.getRootNode().setOnMouseClicked(tileListener(tile));
                tile.clear();
                tile.setSymbol("");

            }
        }
        
        /* Add the pieces */
        for (Map.Entry<Piece, Position> p : pieces.entrySet()) {
            Position placeAt = p.getValue();
            getTileAt(placeAt).setSymbol(TavleiPieceSymbols.getSymbol((TavleiPiece) p.getKey()));
        }
        /* Add the coordinates around the perimeter */
        for (int i = 1; i <= boardSize; i++) {
            Text coord1 = new Text((char) (boardSize * boardSize + i) + "");
            GridPane.setHalignment(coord1, HPos.CENTER);
            gridPane.add(coord1, i, 0);

            Text coord2 = new Text((char) (boardSize * boardSize + i) + "");
            GridPane.setHalignment(coord2, HPos.CENTER);
            gridPane.add(coord2, i, boardSize + 1);

            Text coord3 = new Text(boardSize + 1 - i + "");
            GridPane.setHalignment(coord3, HPos.CENTER);
            gridPane.add(coord3, 0, i);

            Text coord4 = new Text(boardSize + 1 - i + "");
            GridPane.setHalignment(coord4, HPos.CENTER);
            gridPane.add(coord4, boardSize + 1, i);
        }
    }

    /**
     * Gets the view to add to the scene graph
     *
     * @return A pane that is the node for the chess board
     */
    @Override
    public Pane getView() {
        return gridPane;
    }

    /**
     * Gets the tiles that belong to this board view
     *
     * @return A 2d array of TileView objects
     */
    private Tile[][] getTiles() {
        return tiles;
    }

    private Tile getTileAt(int row, int col) {
        return getTiles()[row][col];
    }

    private Tile getTileAt(Position p) {
        return getTileAt(p.getRow(), p.getCol());
    }

    @Override
    public void adviceNextMove() {
        adviseThread.submit(() -> {
            Move adviseMove = getCurrentController().adviceNextMove();
            adviseMove(adviseMove);
        });
    }

    public Side getMySide() {
        return mySide;
    }

    @Override
    public void setMySide(Side mySide) {
        this.mySide = mySide;
    }
}
