package gamemechanics.controller.ai.tavlei;

import com.sun.istack.internal.NotNull;
import entity.Move;
import entity.Side;
import entity.SideHasNoMovesException;
import gamemechanics.model.tavlei.TavleiBoard;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Thread.currentThread;

/**
 * Created by Irina.
 */

@ThreadSafe
class TavleiBoardState implements Comparable<TavleiBoardState> {

    static TavleiBoardState createTavleiBoardState(TavleiBoard board, Move lastMove, Side side) {
        return new TavleiBoardState(board, lastMove, side);

    }

    static TavleiBoardState createTavleiBoardState(TavleiBoard board, Side side) {
        return createTavleiBoardState(board, null, side);
    }


    static class BoardSet extends ArrayList<TavleiBoardState> {
        BoardSet() {
            super();
        }

        /**
         * Get board state according to i rang in sorted list.
         * So from {-10,10,20,30,40,50} for rang 1 it return 40. For rang=0 return 0
         */

        TavleiBoardState getRanged(int i) {
            if (i < 0) i = 0;
            if (i == 0) return getMax();
            if (i >= this.size()) return this.getMin();
            return this.sort().get(i);
        }

        TavleiBoardState getMax() {
            //noinspection OptionalGetWithoutIsPresent
            return this.stream().max(TavleiBoardState::compareTo).get();
        }

        TavleiBoardState getMin() {
            //noinspection OptionalGetWithoutIsPresent
            return this.stream().min(TavleiBoardState::compareTo).get();
        }

        static int inverseComparator(TavleiBoardState o1, TavleiBoardState o2) {
            return o2.compareTo(o1);
        }

        BoardSet sort() {
            super.sort(BoardSet::inverseComparator);
            return this;
        }


        @SuppressWarnings("UnusedReturnValue")
        BoardSet subHead(int i) {
            if (i >= size()) {
                this.clear();
                return this;
            }
            if (i == 1) {
                TavleiBoardState result = this.getMax();
                this.clear();
                this.add(result);
                return this;
            }
            this.sort();
            this.removeRange(i - 1, this.size() - 1);
            return this;
        }
    }

    final static short M = 1000;
    private final TavleiBoard board;
    private final Move lastMove;
    private double weight = 0;
    private double myOwnWeigh = 0;
    private final Side side;
    //Parent thread for checking interruption
    private Thread parent;

    private final BoardSet nextBoards;

    private TavleiBoardState(TavleiBoard board, Move lastMove, Side side) {
        this.board = board;
        this.lastMove = lastMove;
        this.side = side;
        this.weight = (getBoardWeight(board, side));
        this.myOwnWeigh = weight;
        nextBoards = new BoardSet();
    }

    @SuppressWarnings("UnusedReturnValue")
    double evalDeepWeight(int roodDeep) {
        //TODO Check if the thread group is main
        return evalDeepWeight(roodDeep, roodDeep, currentThread());
    }


    private void generateNextBoards() {
        synchronized (this) {
            try {
                board.generateAllMovesForSide(side.getOppositeSide()).forEach((key, value) -> value.forEach(move -> {
                    TavleiBoard lBoard = board.clone();
                    lBoard.movePiece(key, move);
                    TavleiBoardState tavleiBoardState = createTavleiBoardState(lBoard, move, side.getOppositeSide());
                    nextBoards.add(tavleiBoardState);
                }));
            } catch (SideHasNoMovesException e) {
                //do nothing because strike weight=0;
            }
        }
    }

    private void interruptionPoint() {
        if (this.parent.isInterrupted() || !this.parent.isAlive())
            throw new RuntimeException("Thread will be stopped.");
    }

    private double evalEstimateWeight(int limit) {

        if (abs(weight) >= M) return weight;
        //generate Opponent possible moves
        generateNextBoards();
        if (nextBoards.isEmpty()) return weight;
        //cut non profit opponent passes
        nextBoards.subHead(limit);
        //take into account opponent passes to weight
        return weight = balanceWeight(myOwnWeigh);
    }

    private double balanceWeight(double originalWeight) {
        double f = 0.683;
        if (nextBoards.size() == 1) {
            return originalWeight * (1 - f) - f * nextBoards.getMax().getWeight();
        }
        double sum = nextBoards.stream().mapToDouble(TavleiBoardState::getWeight).sum();
        double sumSqr = nextBoards.stream().mapToDouble(value -> Math.pow(value.getWeight(), 2)).sum();

        return originalWeight * (1 - f) - f * sumSqr / sum;
    }

    /**
     * Eval passes from "root" state
     */
    private double evalDeepWeight(int deep, int roodDeep, Thread parentThread) {
        this.parent = parentThread;
        interruptionPoint();
        if (deep == 0) return weight;
        if (abs(weight) >= M) return weight;
        final int limit = (roodDeep - deep < 2) ? 3 : 1;
        //estimate and cut passes
        generateNextBoards();
        if (nextBoards.isEmpty())
            return weight;
        //nextBoards.subHead(limit*20);

        nextBoards.parallelStream().forEach(boardState -> boardState.evalEstimateWeight(limit));
        nextBoards.subHead(limit);

        //eval rest passes...
        if (deep > 1) {

            nextBoards.stream().parallel().forEach(boardState -> {
                        boardState.nextBoards.parallelStream().forEach(newRoot -> newRoot.evalDeepWeight(deep - 1, roodDeep, parent)
                        );
                        //take into account new weight
                        boardState.weight = boardState.balanceWeight(boardState.myOwnWeigh);
                    }

            );
        }
        //include possible passes in the current weight
        if (roodDeep != deep) weight = balanceWeight(myOwnWeigh);
        return weight;
    }

    protected float getBoardWeight(TavleiBoard board, Side s) {
        if (s == Side.BLACK) return getWeightAttacker(board);
        else return getWeightDefender(board);
    }

    protected float getWeightAttacker(TavleiBoard board) {
        return -getWeightDefender(board);

    }

    protected float getWeightDefender(TavleiBoard board) {
        if (board.isWin(Side.WHITE)) return M;
        if (board.isWin(Side.BLACK)) return -M;
        float countAttacker = board.getBlackPositions().size();
        float countDefender = board.getWhitePositions().size();
        if (board.getKingPosition().getRow() == board.getSize() || board.getKingPosition().getCol() == board.getSize())
            countDefender += 0.5;
        //King's passes more preferable
        //if (lastMove!=null && board.getKingPosition()==lastMove.getDestination()) countDefender+=0.001;
        if (countAttacker == 0) return M;

        return countDefender / countAttacker;
    }

    Move getLastMove() {
        return lastMove;
    }

    double getWeight() {
        return weight;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(@NotNull TavleiBoardState  o) {
        if (o==null) throw new RuntimeException("Parameter TavleiBoardState can not be null");
        if (this.weight == o.weight) return 0;
        return this.weight < o.weight ? -1 : 1;
    }

    BoardSet getNextBoards() {
        return nextBoards;
    }

    @Override
    public String toString() {
        return super.toString() + "->" + (lastMove != null ? lastMove.toString() : "null") + "->" + weight;
    }
}
