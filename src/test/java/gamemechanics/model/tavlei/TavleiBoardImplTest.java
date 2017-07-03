package gamemechanics.model.tavlei;

import entity.Move;
import entity.Position;
import entity.Side;
import gamemechanics.model.Piece;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Irina 14.08.2016.
 */
@SuppressWarnings("FieldCanBeLocal")
public class TavleiBoardImplTest {

    private TavleiBoard tavleiboard;
    @SuppressWarnings("CanBeFinal")
    private byte size = 9;
    @Rule
    public Timeout timeout = Timeout.millis(200);

    @Before
    public void setUp() {
        tavleiboard = new TavleiBoardImpl();
        PieceFabric pieceFabric = new PieceFabric(tavleiboard::outOfBound);
        //
        tavleiboard.replacePieceAt(new Position(1, 5), pieceFabric.newRook(Side.WHITE));
        tavleiboard.replacePieceAt(new Position(1, 3), pieceFabric.newRook(Side.WHITE));
        tavleiboard.replacePieceAt(new Position(1, 4), pieceFabric.newRook(Side.BLACK));
        //surround King
        tavleiboard.replacePieceAt(new Position(3, 4), pieceFabric.newRook(Side.BLACK));
        tavleiboard.replacePieceAt(new Position(5, 4), pieceFabric.newRook(Side.BLACK));
        tavleiboard.replacePieceAt(new Position(4, 5), pieceFabric.newRook(Side.BLACK));
        tavleiboard.replacePieceAt(new Position(4, 2), pieceFabric.newRook(Side.BLACK));
        tavleiboard.replacePieceAt(new Position(0, 1), pieceFabric.newRook(Side.BLACK));

    }

    @Test
    public void getSize() throws Exception {
        assertEquals(tavleiboard.getSize(), size);
    }

    @Test
    public void generateAllMovesForSide() throws Exception {
        Map<Piece, Set<Move>> possibleMovies;

        tavleiboard.movePiece(tavleiboard.getPieceAt(new Position(1, 3)), new Move(new Position(1, 3), new Position(1, 2)));
        possibleMovies = tavleiboard.generateAllMovesForSide(Side.WHITE);
        boolean result = possibleMovies.get(tavleiboard.getPieceAt(new Position(1, 2))).stream().anyMatch(move -> move.getDefeated().contains(new Position(1, 4)));
        assertTrue(result);
    }

    @Test
    public void pieceCanMove() throws Exception {
        assertTrue(tavleiboard.pieceCanMove(new Move(new Position(3, 4), new Position(3, 5)), Side.BLACK));
        assertTrue(tavleiboard.pieceCanMove(new Move(new Position(4, 2), new Position(8, 2)), Side.BLACK));
        assertTrue(tavleiboard.pieceCanMove(new Move(new Position(4, 6), new Position(8, 6)), Side.WHITE));

        assertTrue(tavleiboard.pieceCanMove(new Move(new Position(4, 2), new Position(0, 2)), Side.BLACK));

    }

    @Test
    public void isWinDefender() throws Exception {
        assertFalse(tavleiboard.isWin(Side.BLACK));
        tavleiboard.movePiece(tavleiboard.getPieceAt(new Position(4, 2)), new Move(new Position(4, 2), new Position(4, 3)));
        assertTrue(tavleiboard.isWin(Side.BLACK));
    }

    @Test
    public void isInCheckAttacker() throws Exception {
        assertFalse(tavleiboard.isWin(Side.WHITE));
        tavleiboard.movePiece(tavleiboard.getPieceAt(new Position(4, 4)), new Move(new Position(4, 4), new Position(0, 0)));
        assertTrue(tavleiboard.isWin(Side.WHITE));
    }

    @Test
    public void isPieceAt() throws Exception {
        assertTrue(tavleiboard.isPieceAt(new Position(1, 5), Side.WHITE));
    }

    @Test
    public void getSideFromPosition() throws Exception {
        assertEquals(tavleiboard.getSideFromPosition(new Position(4, 4)), Side.WHITE);
        assertEquals(tavleiboard.getSideFromPosition(new Position(4, 4)), Side.WHITE);

    }

}