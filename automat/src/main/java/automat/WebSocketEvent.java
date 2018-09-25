package automat;

import automat.events.CloseEvent;
import automat.events.MessageEvent;
import automat.events.OpenEvent;

import java.util.function.Consumer;

public abstract class WebSocketEvent<T> {

    private final WebSocketEndpoint<T> endpoint;

    public WebSocketEvent(WebSocketEndpoint<T> endpoint) {
        this.endpoint = endpoint;
    }

    public <E extends WebSocketEvent<T>> E acceptOpenEventConsumer(Consumer<OpenEvent<T>> consumer) {
        WebSocketEventHandler<T> handler = new WebSocketEventHandler<T>() {
            @Override
            public void handle(OpenEvent<T> event) {
                consumer.accept(event);
            }
        };
        return accept(handler);
    }

    public <E extends WebSocketEvent<T>> E acceptMessageEventConsumer(Consumer<MessageEvent<T>> consumer) {
        WebSocketEventHandler<T> handler = new WebSocketEventHandler<T>() {
            @Override
            public void handle(MessageEvent<T> event) {
                consumer.accept(event);
            }
        };
        return accept(handler);
    }

    public <E extends WebSocketEvent<T>> E acceptCloseEventConsumer(Consumer<CloseEvent<T>> consumer) {
        WebSocketEventHandler<T> handler = new WebSocketEventHandler<T>() {
            @Override
            public void handle(CloseEvent<T> event) {
                consumer.accept(event);
            }
        };
        return accept(handler);
    }

    public abstract <E extends WebSocketEvent<T>> E accept(WebSocketEventHandler<T> handler);

    public WebSocketEndpoint<T> endPoint() {
        return endpoint;
    }

}


