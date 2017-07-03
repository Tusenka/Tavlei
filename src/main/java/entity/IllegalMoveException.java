package entity;

/**
 * Thrown if a player attempts a move that the board deems illegal
 *
 * @author Joe
 */
public class IllegalMoveException extends GameRulesException {

    /**
     * @param m The illegal move
     */
    public IllegalMoveException(Move m) {
        super("Illegal move: " + m.toString());
    }

    /**
     * @param m The illegal move
     */
    @SuppressWarnings("WeakerAccess")//It is and exception.
    public IllegalMoveException(String s, Move m) {
        super("Illegal move: " + m.toString() + ". " + s);
    }
}