package entity.event;

/**
 * Created by Irina
 */
public enum GameMechanicEventType {
    TEXT_MESSAGE,
    BEGIN_TURN, //Begin turn.
    PROPOSE_MOVE,//Human move. Player (usually - human) propose a move, controllers received the move, which those waiting for. And then make there move, producing MOVE Event
    MOVE,//Controllers complete move. Move already checked and perform. Do not mess with PROPOSE_MOVE.
    END_TURN,//End turn.  Claim about completing turn
    SIDE_CHANGE,
    STATE_CHANGE,
    GAME_OVER,
    GAME_RULES_ERROR
}
