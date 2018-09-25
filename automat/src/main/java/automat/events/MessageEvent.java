package automat.events;

import automat.WebSocketEndpoint;
import automat.WebSocketEvent;
import automat.WebSocketEventHandler;


public class MessageEvent<T> extends WebSocketEvent<T> {
    private T message;

    public MessageEvent(WebSocketEndpoint<T> endpoint, T message) {
        super(endpoint);
        this.message = message;
    }

    @Override
    public <E extends WebSocketEvent<T>> E accept(WebSocketEventHandler<T> handler) {
        handler.handle(this);
        return (E) this;
    }

    public T getMessage() {
        return message;
    }
}
