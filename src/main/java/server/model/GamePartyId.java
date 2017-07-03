package server.model;

import java.util.UUID;

/**
 * Created by Irina
 */
public class GamePartyId {
    private String id;

    private GamePartyId(String id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return id;
    }

    private static String generateStringId() {
        return UUID.randomUUID().toString();
    }

    public static GamePartyId generatePartyId() {
        return new GamePartyId(generateStringId());
    }

    public boolean equals(String str) {
        return this.id.equals(str);
    }

    public boolean equals(GamePartyId gamePartyId) {
        return gamePartyId.id.equals(this.id);
    }

    /*
    * Equals are only supported for String and PlayerId
    */
    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
