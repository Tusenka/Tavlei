package gamemechanics.model.tavlei;

import entity.Move;
import entity.Position;
import entity.Side;
import jdk.nashorn.internal.ir.annotations.Immutable;

import java.util.ArrayList;

/**
 * Rules for Tavlei game. Uses Strategy pattern as a strategy for @{@link TavleiBoardImpl}.
 */
@SuppressWarnings("ALL")
interface TavleiRules {
    @Immutable
    boolean pieceCanMove(final Move m, final Side movingSide);

    ArrayList<Position> getDefeatableFrom(final Position p, final Side s);

    boolean isWin(Side s);

    int getBoardInfoHash();

    TavleiRules clone(TavleiBoard board);
}
