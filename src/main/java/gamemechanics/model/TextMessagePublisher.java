package gamemechanics.model;

import entity.event.TextMessage;

/**
 * Created by Irina
 */
public interface TextMessagePublisher {

    /**
     * Catch and processed by AOP Spring AspectJ.
     */
    @SuppressWarnings("EmptyMethod")
// AspectJ weaving
    void notifyMessage(TextMessage textMessage);
}
