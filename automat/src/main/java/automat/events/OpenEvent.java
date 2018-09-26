package automat.events;

import automat.WebSocketEvent;
import automat.WebSocketEventHandler;
import automat.WebSocketTextEndpoint;

import java.util.function.Function;


public class OpenEvent extends WebSocketEvent<Void> {

    public OpenEvent(WebSocketTextEndpoint endpoint) {
        super(endpoint);
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
