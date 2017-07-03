package server.model;

import entity.Side;
import gamemechanics.controller.ControllerManager;
import generated.GameModeType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Created by Irina
 */
public class GamePartyModelImplTest {
    PlayerId playerId1 = new PlayerId("White");
    PlayerId playerId2 = new PlayerId("Black");
    Side side1 = Side.WHITE;
    Side side2 = Side.BLACK;
    GamePartyModel gamePartyModel;
    ControllerManager mockController;

    @Before
    public void setUp() throws Exception {
        mockController = Mockito.mock(ControllerManager.class);
        Mockito.when(mockController.clone()).thenReturn(mockController);
        Mockito.when(mockController.clone(Mockito.anyObject())).thenReturn(mockController);
        gamePartyModel = new GamePartyModelImpl(mockController);
    }

    @Test
    public void getGamePartyById() throws Exception {
        GameParty gameParty = gamePartyModel.compositeGameParty(playerId1);
        GamePartyId id = gameParty.getPartyId();
        assertSame(gamePartyModel.getGamePartyById(id), gameParty);
    }

    @Test
    public void newGameParty() {
        GameParty gameParty = gamePartyModel.newGameParty(playerId1, side1);
        gameParty.setGameModeType(GameModeType.PLAY_WITH_AI);
        assertEquals(gameParty.getPartyStatus(), PartyStatus.READY);
    }

    @Test
    public void compositeGameParty() throws Exception {
        //noinspection SynchronizeOnNonFinalField It is a test.
        synchronized (gamePartyModel) {
            //destroy GameParty if exists
            GameParty gameParty = gamePartyModel.compositeGameParty(playerId1);
            gamePartyModel.endGameParty(gameParty.getPartyId());
            //Begin new game
            gameParty = gamePartyModel.compositeGameParty(playerId1);
            assertEquals(gameParty.getPartyStatus(), PartyStatus.WAITING_FOR_FILL);
            gamePartyModel.compositeGameParty(playerId2);
            assertEquals(gameParty.getPartyStatus(), PartyStatus.READY);
        }
    }

    @Test
    public void compositeGamePartySide() throws Exception {
        //noinspection SynchronizeOnNonFinalField It is the test.
        synchronized (gamePartyModel) {
            //destroy GameParty
            GameParty gameParty = gamePartyModel.compositeGameParty(playerId1);
            gamePartyModel.endGameParty(gameParty.getPartyId());
            //Begin new game
            gameParty = gamePartyModel.compositeGameParty(playerId1, side1);
            assertEquals(gameParty.getPartyStatus(), PartyStatus.WAITING_FOR_FILL);
            gamePartyModel.compositeGameParty(playerId2, side2);
            assertEquals(gameParty.getPartyStatus(), PartyStatus.READY);
            assertEquals(gameParty.getPlayerIds().get(side1), playerId1);
        }

    }

    @Test
    public void endGameParty() throws Exception {
        GameParty gameParty = gamePartyModel.compositeGameParty(playerId1);
        gamePartyModel.endGameParty(gameParty.getPartyId());
        assertEquals(gameParty.getPartyStatus(), PartyStatus.END);
    }


}