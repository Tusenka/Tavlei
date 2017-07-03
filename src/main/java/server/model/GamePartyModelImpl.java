package server.model;

import entity.Side;
import gamemechanics.controller.ControllerManager;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by Irina
 */
public class GamePartyModelImpl implements GamePartyModel {
    private static ConcurrentHashMap<GamePartyId, GameParty> gameParties = new ConcurrentHashMap<>();
    private static Semaphore compositeLock = new Semaphore(1);

    private ControllerManager templateControllerManager;


    public GamePartyModelImpl(ControllerManager templateControllerManager) {
        this.templateControllerManager = templateControllerManager;
    }

    @Override
    public GameParty getGamePartyById(GamePartyId gamePartyId) {
        if (!gameParties.containsKey(gamePartyId)) gameParties.put(gamePartyId, createGameParty(gamePartyId));
        return gameParties.get(gamePartyId);
    }

    @SuppressWarnings("WeakerAccess")//
    protected GameParty newGameParty() {
        GamePartyId id = GamePartyId.generatePartyId();
        gameParties.put(id, createGameParty(id));
        return gameParties.get(id);
    }

    @Override
    public GameParty newGameParty(PlayerId playerId, Side side) {
        return newGameParty().addPlayerId(playerId, side);
    }

    @Override
    public GameParty compositeGameParty(PlayerId playerId) {
        try {
            compositeLock.tryAcquire(2000, TimeUnit.MILLISECONDS);
            Optional<GameParty> resultOptional = gameParties.values().stream().
                    filter(gameParty -> gameParty.getPartyStatus() == PartyStatus.WAITING_FOR_FILL).
                    findAny();
            GameParty result = (resultOptional.isPresent()) ? resultOptional.get() : newGameParty();
            result.addPlayerId(playerId);
            compositeLock.release();
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
            //return newGameParty();
        }

    }

    @Override
    public GameParty compositeGameParty(PlayerId playerId, Side side) {
        try {
            compositeLock.tryAcquire(2000, TimeUnit.MILLISECONDS);
            Optional<GameParty> resultOptional = gameParties.values().stream().
                    filter(gameParty -> gameParty.getPartyStatus() == PartyStatus.WAITING_FOR_FILL && gameParty.isEmpty(side)).
                    findAny();
            GameParty result = (resultOptional.isPresent()) ? resultOptional.get() : newGameParty();
            result.addPlayerId(playerId, side);
            compositeLock.release();
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * End game party and destroy Event space
     *
     * @param gameId
     */
    @Override
    public void endGameParty(GamePartyId gameId) {
        GameParty gameParty = gameParties.get(gameId);
        if (gameParty != null) gameParty.clear();
        gameParties.remove(gameId);
    }

    @SuppressWarnings("WeakerAccess")
    protected GameParty createGameParty(GamePartyId id) {
        return new GameParty(id, templateControllerManager.clone(id.toString()));
    }


}
