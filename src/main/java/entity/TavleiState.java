package entity;

//State of the game for current game mechanic controller
public enum TavleiState implements GameState {
    WHITE_WINS("Defenders Wins!"),
    BLACK_WINS("Attackers Wins!"),
    STALEMATE("Stalemate"),
    ONGOING("Ongoing"),
    HOSTING("Waiting for a player");

    private final String message;

    TavleiState(String message) {
        this.message = message;
    }

    public boolean isGameOver() {
        return equals(WHITE_WINS) || equals(BLACK_WINS) || equals(STALEMATE);
    }

    @Override
    public String toString() {
        return message;
    }
}
