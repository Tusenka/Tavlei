package gamemechanics.model.tavlei;

import entity.Move;
import entity.Position;
import gamemechanics.model.Piece;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.Set;

import static org.jgroups.util.Util.assertNotNull;
import static org.jgroups.util.Util.assertTrue;

@SuppressWarnings("ALL")
public class KingTest {
    private Piece king;

    @Rule
    public Timeout timeout = Timeout.millis(300);

    @Before
    public void setUp() {
        king = new King(new TavleiBoardImpl()::outOfBound);
    }

    @Test
    public void testMoves() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Set<Move> moves = king.generateMoves(new Position(i, j));
                assertNotNull(moves);
                validateMoves(moves);
            }
        }
    }


    public void validateMoves(Set<Move> moves) {
        for (Move m : moves) {
            int startCol = m.getStart().getCol();
            int startRow = m.getStart().getRow();

            int destCol = m.getDestination().getCol();
            int destRow = m.getDestination().getRow();

            boolean boundsTest =
                    destCol >= 0
                            && destCol < 9
                            && destRow >= 0
                            && destRow < 9;

            int dx = Math.abs(destCol - startCol);
            int dy = Math.abs(destRow - startRow);

            boolean legalityTest = dy < 5 && dx < 5;

            assertTrue(
                    generateErrorMessage("Out of bounds move generated.", m),
                    boundsTest);

            assertTrue(
                    generateErrorMessage("Illegal move generated.", m),
                    legalityTest);
        }
    }

    public String generateErrorMessage(String msg, Move m) {

        return "Error: " +
                msg +
                "\n" +
                m.toString();
    }
}
