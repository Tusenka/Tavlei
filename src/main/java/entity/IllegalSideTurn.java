package entity;

/**
 * Created by Irina
 */
public class IllegalSideTurn extends IllegalMoveException {
    @SuppressWarnings("SameParameterValue")
    public IllegalSideTurn(Side s, Move m) {
        super(s + ".It's not your turn!", m);
    }
}
