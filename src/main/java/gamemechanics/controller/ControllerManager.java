package gamemechanics.controller;

import entity.GameState;
import entity.Side;
import gamemechanics.model.Board;
import gamemechanics.model.event.EventSpaceSupplement;


/**
 * Created by Irina
 * Manage interaction and check turn order among game controllers. It's the highest layer for interaction with game mechanic .
 */
public interface ControllerManager extends EventSpaceSupplement {
    void reset();

    void playWithLocalUser();

    void startGame();

    void stopGame();

    void playWithCompute(Side humanSide);

    void setGameState(GameState gameState);

    void joinToServer(String address);

    void beginTurn();

    GameController getCurrentController();

    void endTurn();

    Side getCurrentSide();

    //for checking events
    void setCurrentSide(Side s);

    Board getBoard();

    GameState getCurrentState();

    ControllerManager clone();

    ControllerManager clone(String newEventSpace);

    default void clear() {
        //Do nothing by default
    }
}
