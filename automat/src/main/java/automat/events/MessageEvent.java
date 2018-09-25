package automat.events;

import automat.WebSocketEvent;
import automat.WebSocketEventHandler;
import automat.WebSocketTextEndpoint;


public class MessageEvent<T> extends WebSocketEvent {
    private T message;

    public MessageEvent(WebSocketTextEndpoint endpoint, T message) {
        super(endpoint);
        this.message = message;
    }

    @Override
    public <E extends WebSocketEvent> E accept(WebSocketEventHandler handler) {
        handler.handle(this);
        return (E) this;
    }

    public T getMessage() {
        return message;
    }
}
