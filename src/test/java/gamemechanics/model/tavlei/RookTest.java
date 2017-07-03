package gamemechanics.model.tavlei;

import entity.Move;
import entity.Position;
import entity.Side;
import gamemechanics.model.Piece;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ALL")
public class RookTest {
    private Piece rook;

    @Rule
    public Timeout timeout = Timeout.millis(200);

    @Before
    public void setUp() {

        rook = new Rook(Side.WHITE, new TavleiBoardImpl()::outOfBound);
    }

    @Test
    public void testMoves() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Set<Move> moves = rook.generateMoves(new Position(i, j));
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

            boolean legalityTest =
                    (Math.abs(destCol - startCol) == 0)

                            ||

                            (Math.abs(destRow - startRow) == 0);

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
