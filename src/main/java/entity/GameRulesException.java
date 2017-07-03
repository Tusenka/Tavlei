package entity;

/**
 * Created by Irina on 24.01.2017.
 */
public class GameRulesException extends RuntimeException {
    @SuppressWarnings("WeakerAccess")//It is Exception, which// theoretically could be generated everywhere...
    public GameRulesException(String message) {
        super(message);
    }
}
