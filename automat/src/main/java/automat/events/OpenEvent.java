package automat.events;

import automat.WebSocketEvent;
import automat.WebSocketEventHandler;
import automat.WebSocketTextEndpoint;


public class OpenEvent extends WebSocketEvent<Void> {

    public OpenEvent(WebSocketTextEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    public WebSocketEvent<Void> accept(WebSocketEventHandler handler) {
        handler.handle(this);
        return this;
    }
}
