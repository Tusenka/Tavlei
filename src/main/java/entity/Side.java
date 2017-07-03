package entity;

/**
 * Represent playing sides for two-player game.
 *
 * @author Joe
 */

public enum Side {

    WHITE("White"),
    BLACK("Black");

    private final String sideName;

    Side(String sideName) {
        this.sideName = sideName;
    }

    public Side getOppositeSide() {
        return this == WHITE ? BLACK : WHITE;
    }

    @Override
    public String toString() {
        return sideName;
    }
}
