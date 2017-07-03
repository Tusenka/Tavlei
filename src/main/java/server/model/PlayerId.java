package server.model;

/**
 * Created by Irina
 */
public class PlayerId {
    private final String id;

    public PlayerId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

    public boolean equals(String str) {
        return this.id.equals(str);
    }

    @SuppressWarnings("WeakerAccess")
    public boolean equals(PlayerId playerId) {
        return playerId.id.equals(this.id);
    }

    /*
    * Equals are only supported for String and PlayerId
    */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlayerId) return this.equals((PlayerId) obj);
        return obj instanceof String && this.equals((String) obj);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
