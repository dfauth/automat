package automat.events;

import automat.WebSocketEvent;
import automat.WebSocketEventHandler;
import automat.WebSocketTextEndpoint;


public class OpenEvent extends WebSocketEvent {

    public OpenEvent(WebSocketTextEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    public <E extends WebSocketEvent> E accept(WebSocketEventHandler handler) {
        handler.handle(this);
        return (E) this;
    }
}
