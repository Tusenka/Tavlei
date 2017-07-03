package gamemechanics.controller.ai.tavlei;


import entity.Move;
import entity.Side;
import gamemechanics.controller.ai.AIAdviser;
import gamemechanics.model.tavlei.TavleiBoard;


import javax.annotation.concurrent.ThreadSafe;
import java.util.Random;

/**
 * Chooses a move for AI and provide advice for human. Advice for human is more "clever"
 *
 * @author Irina
 */
@ThreadSafe
public class SimpleAITavleiEngineI implements AITavleiEngine, AIAdviser {
    @SuppressWarnings("FieldCanBeLocal")
    private static final byte deep = 2;
    @SuppressWarnings("FieldCanBeLocal")
    private static final byte adviseDeep = 4;

    @Override
    public Move chooseNextMove(TavleiBoard board, Side s) {

        TavleiBoardState boardState = TavleiBoardState.createTavleiBoardState(board, s.getOppositeSide());
        boardState.evalDeepWeight(deep);
        TavleiBoardState.BoardSet boards = boardState.getNextBoards();
        Random rand = new Random();
        //int i=(int)abs(rand.nextGaussian()*(board.getSize()-1)/ difficultyLevel);
        //Make "Error" for AI
        int i = rand.nextInt(1);
        if (boards.getMax().getWeight() >= TavleiBoardState.M) i = 0;//if the first pass is check, choose it;
        return boards.getRanged(i).getLastMove();
    }

    public Move adviceNextMove(TavleiBoard board, Side s) {
        TavleiBoardState boardState = TavleiBoardState.createTavleiBoardState(board, s.getOppositeSide());
        boardState.evalDeepWeight(adviseDeep);
        TavleiBoardState.BoardSet boards = boardState.getNextBoards();
        return boards.getMax().getLastMove();
    }
}
