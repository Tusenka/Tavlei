package gamemechanics.model.event;

/**
 * Created by Irina on 16.01.2017.
 */
public interface EventSpaceSupplement {
    String getEventSpace();

    @SuppressWarnings("UnusedReturnValue")
    EventSpaceSupplement setEventSpace(String name);

    EventManager getEventManager();

    void unsubscribe();

    static    aspect Impl {
        private String EventSpaceSupplement.eventSpace;
        private EventManager EventSpaceSupplement.eventManager;
        public String EventSpaceSupplement.getEventSpace() {
            return this.eventSpace;
        }
        public EventSpaceSupplement EventSpaceSupplement.setEventSpace(String EventSpaceName) {
            this.baseSetEventSpace(EventSpaceName);
            return this;
        }
        public EventSpaceSupplement EventSpaceSupplement.baseSetEventSpace(String EventSpaceName) {
            this.eventSpace = EventSpaceName;
            this.eventManager = EventManager.getEventListenerForMe(this.getEventSpace(), this);
            return this;
        }
        public EventManager EventSpaceSupplement.getEventManager() {
            if (this.eventManager == null)
                this.eventManager = EventManager.getEventListenerForMe(this.getEventSpace(), this);
            return this.eventManager;
        }
        public void EventSpaceSupplement.unsubscribe() {
            this.getEventManager().unsubscribeMe();
        }
    }

}
