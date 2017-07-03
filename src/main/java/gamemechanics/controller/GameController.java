package gamemechanics.controller;

import entity.*;

import java.util.Set;

/**
 * Definition of a game mechanic controller interface. Doesn't check turn order. Control game for one side.
 *
 * @author Irina
 */
public interface GameController {
    /**
     * Get the current state of the game
     *
     * @return A GameState instance representing the current state
     */
    GameState getCurrentState();

    /**
     * Starts the game. Do not call this more than once after instantiating
     * the controllerManager.
     */
    void startGame();

    /**
     * Stop the game.
     */
    default void stopGame() {
        //Do Nothing
    }

    //Call this method before destroy controller
    default void clear() {
        //Do Nothing by default
    }

    /**
     * Call this method after calling start game and after a call to end turn.
     * This generate the valid moves for the side that began the turn.
     * Begin turn must be followed by an end turn.
     */
    void beginTurn();

    /**
     * End turn must be followed by a begin turn.
     * Do not perform main job. Do not choose turn on this stage.
     * <p>
     * void choseTurn();
     * <p>
     * <p>
     * /**
     */
    void endTurn();

    /**
     * Gets the set of possible moves for a piece at a particular position.
     * Should return an empty set if the piece has no valid moves.
     *
     * @param p Position in question
     * @return A set of moves or an empty set.
     */
    Set<Move> getMovesForPieceAt(Position p);

    /**
     * Attempts to make a move on the board
     *
     * @param possibleMove The move
     * @throws IllegalMoveException if the move is not in the set of legal moves
     */
    void makeMove(Move possibleMove) throws IllegalMoveException;

    /**
     * Gets the currently playing side.
     *
     * @return
     */
    Side getMySide();

    /**
     * Advise next move
     *
     * @return
     */
    Move adviceNextMove();


}
