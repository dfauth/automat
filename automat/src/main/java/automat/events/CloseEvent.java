package automat.events;

import automat.WebSocketEvent;
import automat.WebSocketEventHandler;
import automat.WebSocketTextEndpoint;

import javax.websocket.CloseReason;
import java.util.function.Function;


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

    @Override
    public <E extends WebSocketEvent<U>,U> E copy(Function<Void, U> f) {
        return (E) this;
    }
}
