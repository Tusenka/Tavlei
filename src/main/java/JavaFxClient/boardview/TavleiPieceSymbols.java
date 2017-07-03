package JavaFxClient.boardview;

import entity.Side;
import gamemechanics.model.tavlei.TavleiPiece;
import gamemechanics.model.tavlei.TavleiPieceType;

/**
 * Created by Irina
 */
public enum TavleiPieceSymbols {
    ROOK("♜", "♖"), KING("♚", "♔");

    private final String black;
    private final String white;

    TavleiPieceSymbols(String black, String white) {
        this.black = black;
        this.white = white;
    }

    private String getSymbol(Side pieceSide) {
        return pieceSide == Side.WHITE ? white : black;
    }

    private static String getSymbol(TavleiPieceType pieceType, Side side) {
        return TavleiPieceSymbols.valueOf(pieceType.toString()).getSymbol(side);
    }

    public static String getSymbol(TavleiPiece piece) {
        return getSymbol((TavleiPieceType) piece.getType(), piece.getSide());
    }
}
