package gamemechanics.model.tavlei;

import entity.Move;
import entity.Position;
import entity.Side;
import gamemechanics.model.Piece;
import gamemechanics.model.PieceType;

import java.util.Set;


@SuppressWarnings("ALL")
public abstract class TavleiPiece implements Piece {

    private static int refCount;

    private final int id;
    @SuppressWarnings("CanBeFinal")
    private Side side;
    @SuppressWarnings("CanBeFinal")
    private TavleiPieceType type;


    public TavleiPiece(TavleiPieceType type, Side side) {
        id = refCount++;
        this.side = side;
        this.type = type;
    }

    @Override
    public abstract Set<Move> generateMoves(Position curPos);

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public Side getSide() {
        return side;
    }

    @Override
    public PieceType getType() {
        return type;
    }

    @Override
    public String toString() {

        return "\nTavleiPiece Type: " +
                type.toString() +
                "\n Side: " +
                side.toString() +
                "\n ID: " +
                id;
    }


}
