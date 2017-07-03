package gamemechanics.model.tavlei;

import entity.Side;

import java.util.function.BiPredicate;

/**
 * Created by Irina 07.09.2016.
 */
class PieceFabric {
    private final BiPredicate<Integer, Integer> outOfBound;

    public PieceFabric(BiPredicate<Integer, Integer> outOfBound) {
        this.outOfBound = outOfBound;
    }

    Rook newRook(Side s) {
        return new Rook(s, outOfBound);
    }

    King newKing() {
        return new King(outOfBound);
    }
}
