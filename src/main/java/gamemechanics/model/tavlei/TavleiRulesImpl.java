package gamemechanics.model.tavlei;

import entity.Move;
import entity.Position;
import entity.Side;
import gamemechanics.model.Piece;
import jdk.nashorn.internal.ir.annotations.Immutable;

import java.util.ArrayList;

/**
 * Created by Irina
 */

class TavleiRulesImpl implements TavleiRules {
    private final TavleiBoard board;

    TavleiRulesImpl(TavleiBoard board) {
        this.board = board;
    }

    @Immutable
    @Override
    public boolean pieceCanMove(final Move m, final Side movingSide) {
        Position dest = m.getDestination();
        Position start = m.getStart();

        Piece mover = board.getPieceAt(start);
        if (mover == null) {
            return false;
        }
        if (mover.getSide() != movingSide) return false;

        boolean positionAvailable = board.isEmptyAt(dest);
        if (!positionAvailable) return false;

        //forbid special fields for all except king
        if (!(board.isKing(mover)) && board.isInSpecialFields(dest)) return false;


        //walk towards destination and check for pieces
        //along the way
        int xi = start.getCol(), xf = dest.getCol();
        int yi = start.getRow(), yf = dest.getRow();

        int dx = 0, dy = 0;
        if (Math.abs(xf - xi) != 0) {
            dx = (xf - xi) / Math.abs(xf - xi);
        }
        if (Math.abs(yf - yi) != 0) {
            dy = (yf - yi) / Math.abs(yf - yi);
        }

        for (int x = xi + dx, y = yi + dy;
             x != xf || y != yf;
             x += dx, y += dy) {
            if (board.getPieceAt(y, x) != null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ArrayList<Position> getDefeatableFrom(Position p, Side s) {

        ArrayList<Position> defeatables = new ArrayList<>();
        Position wallPos;
        Position whatPos;
        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                if ((dy * dx) != 0 || (dy + dx == 0)) continue;
                wallPos = new Position(p.getRow() + dx * 2, p.getCol() + dy * 2);
                whatPos = new Position(p.getRow() + dx, p.getCol() + dy);
                if (isDefeatableBetween(whatPos, wallPos, s)) defeatables.add(whatPos);
            }

        }
        return defeatables;
    }

    @Override
    public boolean isWin(Side s) {

        Position kingPosition = board.getKingPosition();

        //For defenders check if king escapes then victory
        if (s == Side.WHITE) {
            return board.getBlackPositions().size() == 0 || board.isExitPosition(kingPosition);

        } else {
            if (board.getWhitePositions().size() == 0) return true;
            if (!isMoveToKing()) return false;//When king passes it has immune to check

            ArrayList<Position> crossKing = kingPosition.cross();
            if (!board.isKingAtThrone()) {
                for (int i = 0; i < 2; i++) {
                    if (isDefeatableBetweenForKing(crossKing.get(i), crossKing.get(i + 2))) return true;
                }
                return false;
            } else {
                return crossKing.stream().allMatch(position -> isEnemyWallAt(position, Side.WHITE) || board.isThronePosition(position));
            }
        }
    }

    private boolean isMoveToKing() {
        return board.isPieceAtKing(board.getLastMove().getDestination());
    }

    //For future.
    @SuppressWarnings("WeakerAccess")
    protected boolean isFriendWallAt(Position p, Side s) {
        //out of bound is redundant
        return ((board.isInSpecialFields(p) && board.isEmptyAt(p)) || (board.isPieceAt(p, s)));
    }

    @SuppressWarnings("WeakerAccess")
    protected boolean isEnemyWallAt(Position p, Side s) {
        return isFriendWallAt(p, s.getOppositeSide());
    }

    /**
     * Return false for king at or in the throne;
     *
     * @param what
     * @param to
     * @param sideFrom
     * @return
     */
    @SuppressWarnings("WeakerAccess")
    protected boolean isDefeatableBetween(Position what, Position to, Side sideFrom) {
        Side sideWhat = board.getSideFromPosition(what);
        if (sideWhat == null || sideFrom == sideWhat) return false;
        //For king special rules
        return !(board.getPieceAt(what) != null && board.isKing(what)) && (isEnemyWallAt(to, sideWhat));
    }

    public int getBoardInfoHash() {
        if (isMoveToKing()) return board.getLastMove().getDestination().hashCode();
        else return 0;
    }


    @Override
    public TavleiRules clone(TavleiBoard board) {
        return new TavleiRulesImpl(board);
    }

    @SuppressWarnings("WeakerAccess")
    protected boolean isDefeatableBetweenForKing(Position from, Position to) {
        //For king at throne special rules
        if (board.isKingAtThrone()) return false;
        Position lastDestination = board.getLastMove().getDestination();
        return (isEnemyWallAt(to, Side.WHITE) && isEnemyWallAt(from, Side.WHITE) && (lastDestination.equals(from) || lastDestination.equals(to)));
    }
}
