package JavaFxClient.model.event;

import generated.GameMechanicResponse;
import generated.ResponseType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by Irina
 */
//TODO: Consider ability to generalization
public class NetworkEventManager {

    private static final NetworkEventManager eventListener = new NetworkEventManager();

    public static NetworkEventManager getEventListener() {
        return eventListener;
    }

    protected static class CallbackList extends ConcurrentHashMap<String, Map<String, Consumer<GameMechanicResponse>>> {
        CallbackList() {
            super();
        }

        Map<String, Consumer<GameMechanicResponse>> getCallbacks(String name) {
            Map<String, Consumer<GameMechanicResponse>> result = this.get(name);
            if (result == null) result = new HashMap<>();
            return result;
        }

        void addCallback(String name, Consumer<GameMechanicResponse> callback, String id) {
            if (!this.containsKey(name)) this.put(name, new HashMap<>());
            this.get(name).put(id, callback);
        }

        public void removeCallback(String id) {
            this.forEach((s, stringConsumerHashMap) -> stringConsumerHashMap.remove(id));
        }
    }

    private String eventSpace;
    private final CallbackList callbackList = new CallbackList();

    public void addListener(ResponseType responseType, Consumer<GameMechanicResponse> listener, String id) {
        callbackList.addCallback(responseType.toString(), listener, id);
    }

    public void removeListener(String id) {
        callbackList.removeCallback(id);
    }

    public synchronized void triggerEvent(GameMechanicResponse gameEvent) {
        onApplicationEvent(gameEvent);
    }

    private void onApplicationEvent(GameMechanicResponse gameEvent) {
        callbackList.getCallbacks(gameEvent.getType().toString()).forEach((s, gameMechanicResponseConsumer) ->
                gameMechanicResponseConsumer.accept(gameEvent)
        );
    }
}
