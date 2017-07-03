package gamemechanics.model.tavlei;

import entity.Move;
import entity.Position;
import entity.Side;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

class King extends TavleiPiece {
    private final BiPredicate<Integer, Integer> outOfBound;

    King(BiPredicate<Integer, Integer> outOfBound) {
        super(TavleiPieceType.KING, Side.WHITE);
        this.outOfBound = outOfBound;
    }

    @Override
    public Set<Move> generateMoves(Position curPos) {
        Set<Move> moves = new HashSet<>();
        int curCol = curPos.getCol();
        int curRow = curPos.getRow();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (Math.abs(dx) != Math.abs(dy)) {
                    int destCol = curCol + dx;
                    int destRow = curRow + dy;

                    for (int i = 0; (!this.outOfBound.test(destRow, destCol)) && i < 4; i++) {
                        Position destPos = new Position(destRow, destCol);
                        moves.add(new Move(curPos, destPos));
                        destCol = destCol + dx;
                        destRow = destRow + dy;
                    }
                }
            }
        }
        return moves;
    }
}
