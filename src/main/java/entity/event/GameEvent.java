package entity.event;

import entity.Side;

import java.io.Serializable;

/**
 * Created by Irina.
 */
@SuppressWarnings("UnusedReturnValue")
public class GameEvent implements Serializable {
    private Side sourceSide;
    private String type;
    private Object data;

    public GameEvent() {
    }

    public GameEvent(GameMechanicEventType gameMechanicEventType) {
        setType(gameMechanicEventType.toString());
    }

    public GameEvent(GameMechanicEventType gameMechanicEventType, Object data) {
        setType(gameMechanicEventType.toString());
        setData(data);
    }

    public GameEvent setSourceSide(Side sourceSide) {
        this.sourceSide = sourceSide;
        return this;
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public Side getSourceSide() {
        return sourceSide;
    }

    @SuppressWarnings({"WeakerAccess"})
    public GameEvent setType(String type) {
        this.type = type;
        return this;
    }

    public GameEvent setType(GameMechanicEventType type) {
        this.type = type.toString();
        return this;
    }

    public GameEvent setData(Object data) {
        this.data = data;
        return this;
    }


}
