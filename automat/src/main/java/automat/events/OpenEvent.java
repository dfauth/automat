package automat.events;

import automat.WebSocketEndpoint;
import automat.WebSocketEvent;
import automat.WebSocketEventHandler;


public class OpenEvent<T> extends WebSocketEvent<T> {

    public OpenEvent(WebSocketEndpoint<T> endpoint) {
        super(endpoint);
    }

    @Override
    public <E extends WebSocketEvent<T>> E accept(WebSocketEventHandler<T> handler) {
        handler.handle(this);
        return (E) this;
    }
}
