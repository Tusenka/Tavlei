package gamemechanics.controller.ai.tavlei;

import entity.Move;
import entity.Side;
import gamemechanics.model.tavlei.TavleiBoard;

/**
 * An interface that allows you to swap out more and more sophisticated AI
 *
 * @author Irina
 */
public interface AITavleiEngine {

    /**
     * Choose the next move that the computer will make
     *
     * @param board
     * @param side
     * @return the AI's move
     */
    Move chooseNextMove(TavleiBoard board, Side side);
}
