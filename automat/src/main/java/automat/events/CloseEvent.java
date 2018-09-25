package automat.events;

import automat.WebSocketEndpoint;
import automat.WebSocketEvent;
import automat.WebSocketEventHandler;

import javax.websocket.CloseReason;


public class CloseEvent<T> extends WebSocketEvent<T> {
    private final CloseReason reason;

    public CloseEvent(WebSocketEndpoint<T> endpoint, CloseReason reason) {
        super(endpoint);
        this.reason = reason;
    }

    @Override
    public <E extends WebSocketEvent<T>> E accept(WebSocketEventHandler<T> handler) {
        handler.handle(this);
        return (E) this;
    }
}
