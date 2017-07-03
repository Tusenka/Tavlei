package server.model;

import entity.Side;

/**
 * Created by Irina
 */
public interface GamePartyModel {
    GameParty getGamePartyById(GamePartyId gamePartyId);

    GameParty newGameParty(PlayerId playerId, Side side);

    GameParty compositeGameParty(PlayerId playerId);

    GameParty compositeGameParty(PlayerId playerId, Side side);

    void endGameParty(GamePartyId gameId);
}
