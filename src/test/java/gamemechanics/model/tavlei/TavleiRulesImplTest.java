package gamemechanics.model.tavlei;

import entity.Move;
import entity.Position;
import entity.Side;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Irina 05.09.2016.
 */
public class TavleiRulesImplTest {
    private TavleiBoard tavleiboard;
    private TavleiRulesImpl rules;

    @Rule
    public Timeout timeout = Timeout.millis(200);

    private void setupBoard() {
        tavleiboard = new TavleiBoardImpl();
        PieceFabric pieceFabric = new PieceFabric(tavleiboard::outOfBound);
        //
        tavleiboard.replacePieceAt(new Position(1, 5), pieceFabric.newRook(Side.WHITE));
        tavleiboard.replacePieceAt(new Position(1, 2), pieceFabric.newRook(Side.WHITE));
        tavleiboard.replacePieceAt(new Position(1, 4), pieceFabric.newRook(Side.BLACK));
        //surround King
        tavleiboard.replacePieceAt(new Position(3, 4), pieceFabric.newRook(Side.BLACK));
        tavleiboard.replacePieceAt(new Position(5, 4), pieceFabric.newRook(Side.BLACK));
        tavleiboard.replacePieceAt(new Position(4, 5), pieceFabric.newRook(Side.BLACK));
        tavleiboard.replacePieceAt(new Position(4, 2), pieceFabric.newRook(Side.BLACK));
        tavleiboard.replacePieceAt(new Position(0, 1), pieceFabric.newRook(Side.BLACK));
        tavleiboard.removePiece(tavleiboard.getPieceAt(new Position(4, 3)));
    }

    @Before
    public void setUp() {
        setupBoard();
        rules = new TavleiRulesImpl(tavleiboard);

    }

    @Test
    public void pieceCanMove() throws Exception {
        assertTrue(rules.pieceCanMove(new Move(new Position(3, 4), new Position(3, 5)), Side.BLACK));
        assertTrue(rules.pieceCanMove(new Move(new Position(4, 2), new Position(8, 2)), Side.BLACK));
        assertTrue(rules.pieceCanMove(new Move(new Position(4, 6), new Position(8, 6)), Side.WHITE));
    }

    @Test
    public void isInCheckDefender() throws Exception {

        tavleiboard.movePiece(tavleiboard.getPieceAt(new Position(3, 4)), new Move(new Position(3, 4), new Position(3, 4)));
        assertFalse(rules.isWin(Side.BLACK));
        tavleiboard.movePiece(tavleiboard.getPieceAt(new Position(4, 2)), new Move(new Position(4, 2), new Position(4, 3)));
        assertTrue(rules.isWin(Side.BLACK));
    }

    @Test
    public void isInCheckAttacker() throws Exception {
        assertFalse(tavleiboard.isWin(Side.WHITE));
        tavleiboard.movePiece(tavleiboard.getPieceAt(new Position(4, 4)), new Move(new Position(4, 4), new Position(0, 0)));
        assertTrue(rules.isWin(Side.WHITE));
    }

    @Test
    public void getDefeatableFrom() throws Exception {
        assertTrue(rules.getDefeatableFrom(new Position(0, 2), Side.WHITE).contains(new Position(0, 1)));
    }

    @Test
    public void isInCheck() throws Exception {
        assertFalse(rules.isWin(Side.BLACK));
        tavleiboard.movePiece(tavleiboard.getPieceAt(new Position(0, 3)), new Move(new Position(0, 3), new Position(4, 3)));
        assertTrue(rules.isWin(Side.BLACK));
    }

    @Test
    public void isFriendWallAt() throws Exception {
        assertTrue(rules.isFriendWallAt(new Position(0, 0), Side.WHITE));
    }

    @Test
    public void isEnemyWallAt() throws Exception {
        assertTrue(rules.isEnemyWallAt(new Position(4, 4), Side.BLACK));

    }

    @Test
    public void isDefeatableBetween() throws Exception {
        assertTrue(rules.isDefeatableBetween(new Position(1, 5), new Position(0, 5), Side.BLACK));
    }

    @Test
    public void isDefeatableBetweenForKing() throws Exception {
        tavleiboard.movePiece(tavleiboard.getPieceAt(new Position(4, 4)), new Move(new Position(4, 4), new Position(8, 7)));
        tavleiboard.movePiece(tavleiboard.getPieceAt(new Position(8, 5)), new Move(new Position(8, 5), new Position(8, 6)));
        assertTrue(rules.isDefeatableBetweenForKing(new Position(8, 6), new Position(8, 8)));

    }

}