package gamemechanics.model.event;


import entity.event.GameEvent;
import entity.event.GameMechanicEventType;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by Irina on 27.11.2016.
 * Event manager uses pattern Fabric, Decorator
 * Uses pattern Decorator with @{@link EventManagerMock}
 */
//TODO: Think about generalization with networkEventManager
public class EventManager {

    private static final ConcurrentHashMap<String, EventManager> eventListeners = new ConcurrentHashMap<>();
    private static final String defaultEventSpace = "default";
    String holderId = "permanent";

    public static EventManager getDefaultEventListener() {
        return getEventListener(defaultEventSpace);
    }

    protected static EventManager getEventListener(String eventSpace) {
        if (eventSpace == null) eventSpace = defaultEventSpace;
        if (!eventListeners.containsKey(eventSpace)) putEventListener(eventSpace, new EventManager(eventSpace));
        return eventListeners.get(eventSpace);
    }

    private static void putEventListener(String eventSpace, EventManager eventManager) {
        eventListeners.put(eventSpace, eventManager);
    }

    public static EventManager getEventListenerForMe(String eventSpace, Object holder) {
        if (eventSpace == null) eventSpace = defaultEventSpace;
        return new EventManagerMock(eventSpace, holder.getClass().getSimpleName() + System.identityHashCode(holder));
    }

    protected static class CallbackList extends ConcurrentHashMap<String, List<Consumer<GameEvent>>> {
        private final ConcurrentHashMap<String, Consumer> consumerHolders = new ConcurrentHashMap<>();

        CallbackList() {
            super();
        }

        List<Consumer<GameEvent>> getCallbacks(String name) {
            List<Consumer<GameEvent>> result = this.get(name);
            if (result == null) result = new LinkedList<>();
            return result;
        }

        boolean addCallback(String name, String holderId, Consumer<GameEvent> callback) {
            if (!this.containsKey(name)) this.put(name, new LinkedList<>());
            consumerHolders.put(holderId, callback);
            return this.get(name).add(callback);
        }

        boolean removeCallback(String holderId) {
            this.forEach((s, consumers) -> consumers.removeIf(gameEventConsumer -> gameEventConsumer == consumerHolders.get(holderId) || gameEventConsumer == null));
            return true;
        }

    }

    @SuppressWarnings("FieldCanBeLocal")
//Information about it's own eventSpace could be used in debugging... I think, it is better to remain it here
    private final String eventSpace;
    private final CallbackList callbackList = new CallbackList();

    public void addListener(GameMechanicEventType event, Consumer<GameEvent> listener) {
        addListener(event.toString(), listener);
    }

    public void addListener(String event, Consumer<GameEvent> listener) {

        callbackList.addCallback(event, this.holderId, listener);
    }

    protected void addListener(String event, String holderId, Consumer<GameEvent> listener) {

        callbackList.addCallback(event, holderId, listener);
    }

    public void unsubscribeMe() {
        //Do nothing on permanent EventSpace

    }

    protected void removeListener(String holderId) {
        this.callbackList.removeCallback(holderId);
    }

    public static void destroyEventSpace(String key) {
        eventListeners.remove(key);
    }

    public synchronized void triggerEvent(GameEvent gameEvent) {
        onApplicationEvent(gameEvent);
    }

    public void onApplicationEvent(GameEvent gameEvent) {
        callbackList.getCallbacks(gameEvent.getType()).forEach(
                consumer -> consumer.accept(gameEvent)
        );
    }

    public String getEventSpace() {
        return eventSpace;
    }

    private EventManager(String eventSpace) {
        this.eventSpace = eventSpace;
    }

    /**
     * Use pattern Decorator. Use for getting personal unsubscribable EventManager for an Object
     */
    public static class EventManagerMock extends EventManager {
        private final EventManager eventManager;

        private EventManagerMock(String eventSpace, String holderId) {
            super(eventSpace);
            this.holderId = holderId;
            eventManager = EventManager.getEventListener(eventSpace);
        }

        @Override
        public void onApplicationEvent(GameEvent gameEvent) {
            eventManager.onApplicationEvent(gameEvent);
        }

        @Override
        public void addListener(GameMechanicEventType event, Consumer<GameEvent> listener) {
            eventManager.addListener(event.toString(), holderId, listener);
        }

        @Override
        public void unsubscribeMe() {
            eventManager.removeListener(holderId);
        }

        @Override
        public void addListener(String event, Consumer<GameEvent> listener) {
            eventManager.addListener(event, holderId, listener);
        }

        @Override
        public void triggerEvent(GameEvent gameEvent) {
            eventManager.triggerEvent(gameEvent);
        }

    }

}
