package automat.events;

import automat.WebSocketEvent;
import automat.WebSocketEventHandler;
import automat.WebSocketTextEndpoint;

import javax.websocket.CloseReason;


public class CloseEvent extends WebSocketEvent<Void> {
    private final CloseReason reason;

    public CloseEvent(WebSocketTextEndpoint endpoint, CloseReason reason) {
        super(endpoint);
        this.reason = reason;
    }

    @Override
    public WebSocketEvent<Void> accept(WebSocketEventHandler handler) {
        handler.handle(this);
        return this;
    }
}
