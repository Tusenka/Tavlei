package gamemechanics.controller.tavlei;

import entity.*;
import entity.Logger.LoggableI;
import entity.event.TextMessage;

import gamemechanics.controller.ai.AIAdviser;
import gamemechanics.controller.ai.tavlei.SimpleAITavleiEngineI;
import gamemechanics.model.Piece;
import gamemechanics.model.TextMessagePublisher;
import gamemechanics.model.event.EventSpaceSupplement;
import gamemechanics.model.tavlei.TavleiBoard;
import gamemechanics.model.tavlei.TavleiBoardImpl;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Controls a game for a player
 *
 * @author Irina
 */
@Configurable
abstract public class TavleiControllerImpl implements TavleiController, EventSpaceSupplement, TextMessagePublisher, LoggableI {

    private final AIAdviser adviser;
    private TavleiBoard board;
    protected Side mySide;
    private Map<Piece, Set<Move>> currentMoves;
    private Piece selectedPiece;
    private GameState currentState;

    protected TavleiControllerImpl() {
        this(Side.BLACK, new TavleiBoardImpl());
    }

    protected TavleiControllerImpl(Side s, TavleiBoard board) {
        setCurrentState(TavleiState.ONGOING);
        setBoard(board);
        setMySide(s);
        setCurrentMoves(new HashMap<>());
        adviser = new SimpleAITavleiEngineI();
    }

    @Override
    public GameState getCurrentState() {
        return currentState;
    }

    protected void setCurrentState(GameState state) {
        currentState = state;
    }

    @Override
    public void startGame() {
        currentState = TavleiState.ONGOING;
        beginTurn();
    }

    @Override
    public void beginTurn() {
        if (getCurrentState().isGameOver()) {
            setCurrentMoves(null);
            return;
        }
        try {
            setCurrentMoves(getBoard().generateAllMovesForSide(getMySide()));
        } catch (SideHasNoMovesException e) {
            setCurrentMoves(null);
            setCurrentState(TavleiState.STALEMATE);
        }
    }

    protected GameState checkGameState() {
        if (getBoard().isWin(getMySide())) {
            setCurrentState((getMySide().equals(Side.WHITE))
                    ? TavleiState.WHITE_WINS : TavleiState.BLACK_WINS);
        }
        return getCurrentState();
    }

    @Override
    public void endTurn() {
        checkGameState();
    }


    @Override
    public Set<Move> getMovesForPieceAt(Position p) {
        Piece piece = getBoard().getPieceAt(p);
        return (piece == null || piece.getSide() != getMySide())
                ? new HashSet<>() : getCurrentMoves().get(piece);
    }

    @Override
    public void makeMove(Move possibleMove) throws IllegalMoveException {
        Piece mover = getBoard().getPieceAt(possibleMove.getStart());
        selectedPiece = mover;
        Move move = getMove(possibleMove);
        if (move == null) {
            throw new IllegalMoveException(possibleMove);
        }
        possibleMove.setDefeated(move.getDefeated());

        if (getCurrentMoves() != null &&
                getCurrentMoves().get(mover) != null &&
                getCurrentMoves().get(mover).contains(possibleMove)) {
            getBoard().movePiece(mover, move);

        } else {
            throw new IllegalMoveException(possibleMove);
        }
    }

    @Override
    public void notifyMessage(TextMessage textMessage) {
        //For aspect implementation
    }

    public Side getMySide() {
        return mySide;
    }


    @SuppressWarnings("WeakerAccess")//setMySide available for all TavleiControllerImpl children
    public void setMySide(Side mySide) {
        this.mySide = mySide;
    }

    @Override
    public Move adviceNextMove() {
        return adviser.adviceNextMove(board, mySide);
    }

    protected TavleiBoard getBoard() {
        return board;
    }

    protected void setBoard(TavleiBoard board) {
        this.board = board;
    }

    @SuppressWarnings("WeakerAccess")
    protected Map<Piece, Set<Move>> getCurrentMoves() {
        return currentMoves;
    }

    private void setCurrentMoves(Map<Piece, Set<Move>> currentMoves) {
        this.currentMoves = currentMoves;
    }

    @SuppressWarnings("WeakerAccess")
    protected Move getMove(Move m) {
        if (currentMoves == null || selectedPiece == null || currentMoves.get(selectedPiece) == null) {
            return null;
        }
        for (Move test : currentMoves.get(selectedPiece)) {
            if (m.equals(test)) {
                return test;
            }
        }
        return null;
    }

    @Override
    public void clear() {
        //For aspectJ implementation
    }

}
