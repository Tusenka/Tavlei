package gamemechanics.model.tavlei;

import entity.Move;
import entity.Position;
import entity.Side;
import gamemechanics.model.Board;
import gamemechanics.model.Piece;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Irina
 */
public interface TavleiBoard extends Board, Cloneable {


    Map<Piece, Position> getWhitePositions();

    Map<Piece, Position> getBlackPositions();

    Position getThrone();

    ArrayList<Position> getExits();

    Move getLastMove();

    Move addInfoMove(Move m, Side s);

    boolean outOfBound(int row, int col);

    Position getKingPosition();

    /**
     * @param s
     * @return If side s is winning
     */
    boolean isWin(Side s);

    TavleiBoard clone();

    @SuppressWarnings("unused")
    void setRules(TavleiRules rules);

    /*
    * Helper methods, usually used for Rules
    */
    boolean isKingAtThrone();

    /*
    * Return if the Piece is near King
    * */
    boolean isPieceAtKing(Position p);

    boolean isExitPosition(Position p);

    boolean isThronePosition(Position p);

    boolean isEmptyAt(Position p);

    boolean isPieceAt(Position p, Side s);

    Side getSideFromPosition(Position p);

    boolean isKing(Piece piece);

    boolean isKing(Position pos);

    boolean isInSpecialFields(Position p);

    /**
     * Helper method for fast checking if the piece exists at the positions
     */
    Piece getPieceAt(int row, int col);
}
