package server.model;

import entity.GameState;
import entity.Move;
import entity.Side;
import entity.event.GameEvent;
import entity.event.GameMechanicEventType;
import gamemechanics.controller.ControllerManager;
import gamemechanics.model.event.EventManager;
import gamemechanics.model.event.EventSpaceSupplement;
import generated.GameModeType;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;

/**
 * Created by Irina on 18.01.2017.
 */
@SuppressWarnings("UnusedReturnValue")
public class GameParty implements EventSpaceSupplement {
    private GamePartyId gamePartyId;
    private final EnumMap<Side, PlayerId> playerIds = new EnumMap<>(Side.class);
    private HashMap<PlayerId, Side> indexPlayerIds = new HashMap<>();
    private PartyStatus partyStatus;
    private GameModeType gameModeType = GameModeType.MULTILAYER;
    private EventManager eventManager;
    private ControllerManager controllerManager;

    public static class NotReading extends RuntimeException {
        NotReading(String message) {
            super(message);
        }

        public NotReading(String message, Throwable cause) {
            super(message, cause);
        }

        public NotReading(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

        NotReading() {
            super("Game party does not reading. Probably, not filling by players");
        }
    }
    public static class IllegalModification extends RuntimeException {
        IllegalModification(String message) {
            super(message);
        }

        public IllegalModification(String message, Throwable cause) {
            super(message, cause);
        }

        public IllegalModification(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

        IllegalModification() {
            super("Illegal modification game party property at this stage ");
        }
    }

    public GamePartyId getPartyId() {
        return gamePartyId;
    }

    public EnumMap<Side, PlayerId> getPlayerIds() {
        return playerIds;
    }

    synchronized GameParty addPlayerId(PlayerId playerId) {
        if (Side.values().length == playerIds.hashCode()) {
            setPartyStatus(PartyStatus.READY);
        }
        Side emptySlot = getEmptySlot();
        addPlayerId(playerId, emptySlot);
        return this;
    }

    synchronized private Side getEmptySlot() {
        return Arrays.stream(Side.values()).filter(this::isEmpty).findAny().orElse(null);
    }

    boolean isEmpty(Side side) {
        return !playerIds.containsKey(side) || playerIds.get(side) == null;
    }

    synchronized GameParty addPlayerId(PlayerId playerId, Side side) {
        this.playerIds.put(side, playerId);
        if (isFilled()) this.partyStatus = PartyStatus.READY;
        return this;
    }

    void clear() {
        this.playerIds.clear();
        controllerManager.clear();
        this.partyStatus = PartyStatus.END;
        EventManager.destroyEventSpace(getEventSpace());
    }

    public ControllerManager getControllerManager() {
        return controllerManager;
    }

    /*For future cash*/
    public GameParty setControllerManager(ControllerManager controllerManager) {
        this.controllerManager = controllerManager;
        return this;
    }

    @SuppressWarnings("WeakerAccess")
    public GameModeType getGameModeType() {
        return gameModeType;
    }

    public GameParty setGameModeType(GameModeType gameModeType) {
        if (partyStatus.compareTo(PartyStatus.READY)>0) throw new IllegalModification("Can not change game mode at " +partyStatus +" status of game party");
        if (this.gameModeType==gameModeType) return this;
        this.gameModeType = gameModeType;
        reCheckPartyStatus();
        return this;
    }

    public PartyStatus getPartyStatus() {
        return partyStatus;
    }

    private GameParty setPartyStatus(PartyStatus partyStatus) {
        this.partyStatus = partyStatus;
        return this;
    }

    private void fillIndexPlayers() {
        indexPlayerIds.clear();
        playerIds.forEach((side, s) -> indexPlayerIds.put(s, side));
    }

    private Side getSide(PlayerId playerId) {
        return indexPlayerIds.get(playerId);
    }

    public void start() throws NotReading {
        if (controllerManager == null) throw new NotReading("Controller manager must be pointed");
        if (this.partyStatus == PartyStatus.WAITING_FOR_FILL )
            throw new NotReading();
        getEventManager().addListener(GameMechanicEventType.STATE_CHANGE, gameEvent -> onStateGameChange((GameState) gameEvent.getData()));
        switch (getGameModeType()) {
            case MULTILAYER:
                controllerManager.playWithLocalUser();
                break;
            case PLAY_WITH_AI: {
                controllerManager.playWithCompute(playerIds.keySet().stream().findAny().orElseThrow(() -> new RuntimeException("Something is really wrong. Starting game was destroyed by unknown reason.")));
            }
            break;
            default:
                throw new NotReading("GameMode " + getGameModeType() + " doesn't supported at present time.");
        }
        fillIndexPlayers();
        this.partyStatus = PartyStatus.ONGOING;
        controllerManager.startGame();

    }

    GameParty(GamePartyId gamePartyId, ControllerManager controllerManager) {
        this.gamePartyId = gamePartyId;
        this.partyStatus = PartyStatus.WAITING_FOR_FILL;
        this.controllerManager = controllerManager;
        setEventSpace(gamePartyId.toString());
        eventManager = getEventManager();
    }

    public void makeMove(PlayerId playerId, Move move) {
        eventManager.triggerEvent(new GameEvent(GameMechanicEventType.PROPOSE_MOVE, move).setSourceSide(getSide(playerId)));
    }

    private void onStateGameChange(GameState gameState) {
        if (gameState.isGameOver()) this.setPartyStatus(PartyStatus.END);
    }

    protected boolean isFilled()
    {
        return (gameModeType == GameModeType.PLAY_WITH_AI) || this.playerIds.size() == Side.values().length;
    }
    private void reCheckPartyStatus()
    {
        if (isFilled())
        {
            this.partyStatus = PartyStatus.READY;
        }
        else
        {
            partyStatus=PartyStatus.WAITING_FOR_FILL;
        }
    }
    @Override
    public String toString() {
        return "Game Party id:" + this.gamePartyId + ", Party status:" + this.partyStatus + "Players:" + this.playerIds.toString();
    }
}
