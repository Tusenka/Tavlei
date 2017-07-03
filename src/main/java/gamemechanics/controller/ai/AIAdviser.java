package gamemechanics.controller.ai;

import entity.Move;
import entity.Side;
import gamemechanics.model.tavlei.TavleiBoard;


/**
 * AI Adviser advise  interface.
 * Created by Irina
 */
public interface AIAdviser {
    Move adviceNextMove(TavleiBoard board, Side currentSide);
}
