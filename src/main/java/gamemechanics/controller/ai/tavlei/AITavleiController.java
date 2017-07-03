package gamemechanics.controller.ai.tavlei;

import entity.IllegalMoveException;
import entity.Move;
import entity.Side;
import gamemechanics.controller.tavlei.TavleiControllerImpl;
import gamemechanics.model.tavlei.TavleiBoard;

/**
 * Simple controller a game between for AI player
 *
 * @author Irina
 */
public class AITavleiController extends TavleiControllerImpl {
    private final AITavleiEngine aiTavleiEngine;


    public AITavleiController(Side aiSide, TavleiBoard board) {
        super(aiSide, board);
        aiTavleiEngine = new SimpleAITavleiEngineI();
    }

    @Override
    public void beginTurn() {
        super.beginTurn();
        if (getMySide() == mySide && !getCurrentState().isGameOver()) {
            Move selected;
            selected = aiTavleiEngine.chooseNextMove(getBoard(), mySide);
            try {
                super.makeMove(selected);
            } catch (IllegalMoveException e) {
                getLog().error("Something is going wrong in AI. Can't perform chosen pass!" + e);
                e.printStackTrace();
            }
            super.endTurn();
        }
    }
}
