package automat.events;

import automat.WebSocketEvent;
import automat.WebSocketEventHandler;
import automat.WebSocketTextEndpoint;

import java.util.function.Function;


public class MessageEvent<T> extends WebSocketEvent<T> {
    private T message;

    public MessageEvent(WebSocketTextEndpoint endpoint, T message) {
        super(endpoint);
        this.message = message;
    }

    @Override
    public WebSocketEvent<T> accept(WebSocketEventHandler<T> handler) {
        handler.handle(this);
        return this;
    }

    public T getMessage() {
        return message;
    }


    @Override
    public <E extends WebSocketEvent<U>,U> E copy(Function<T, U> f) {
        return (E) new MessageEvent<U>(endPoint(), f.apply(message));
    }
}
