package JavaFxClient.controllers;

import gamemechanics.controller.ai.tavlei.AITavleiController;
import gamemechanics.controller.tavlei.LocalUserTavleiController;
import gamemechanics.controller.tavlei.TavleiControllerManager;
import entity.GameRulesException;
import entity.GameState;
import entity.Side;
import entity.TavleiState;
import gamemechanics.controller.tavlei.TavleiController;
import JavaFxClient.model.event.NetworkEventManager;
import entity.event.GameEvent;
import entity.event.GameMechanicEventType;
import generated.ResponseType;
import gamemechanics.model.event.EventManager;
import gamemechanics.model.event.EventSpaceSupplement;
import gamemechanics.model.tavlei.TavleiBoardImpl;

/**
 * Created by Irina
 * Manage interaction and check turn order among game controllers. It's the highest layer for interaction "outer world" with game mechanic layer .
 * Interact by @{@link GameEvent} events.
 */
public class TavleiControllerManagerClient extends TavleiControllerManager {
    public TavleiControllerManagerClient() {
        super(null);
    }

    public TavleiControllerManagerClient(String newEventSpace) {
        super(newEventSpace);
    }

    @Override
    public void joinToServer(String address) {
        reset();
        getControllerThread().submit(() ->
        {
            JavaTavleiClientController javaTavleiClientController = JavaTavleiClientController.getClientController(address,  getBoard());
            NetworkEventManager.getEventListener().removeListener(getClass().getName());
            NetworkEventManager.getEventListener().addListener(ResponseType.PARTNER_FOUNDED, response ->
            {
                javaTavleiClientController.setMySide(response.getYourSide().getOppositeSide());
                controllers.put(response.getYourSide().getOppositeSide(), javaTavleiClientController);//javaTavleiClientController is behalf of LocalUserTavleiController for the Server
                controllers.put(response.getYourSide(), new LocalUserTavleiController(response.getYourSide(), getBoard()));
                setCurrentSide(Side.BLACK);
                startGame();
            }, getClass().getName());
            javaTavleiClientController.startGame();
        });
    }


}
