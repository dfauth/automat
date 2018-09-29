package automat;

import automat.events.CloseEvent;
import automat.events.MessageEvent;
import automat.events.OpenEvent;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class WebSocketEvent<T> {

    private final WebSocketTextEndpoint endpoint;

    public WebSocketEvent(WebSocketTextEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public WebSocketEvent<T> acceptOpenEventConsumer(Consumer<OpenEvent> consumer) {
        WebSocketEventHandler<T> handler = new WebSocketEventHandler<T>() {
            @Override
            public void handle(OpenEvent event) {
                consumer.accept(event);
            }
        };
        return accept(handler);
    }

    public WebSocketEvent<T> acceptMessageEventConsumer(Consumer<MessageEvent<T>> consumer) {
        WebSocketEventHandler<T> handler = new WebSocketEventHandler<T>() {
            @Override
            public void handle(MessageEvent<T> event) {
                consumer.accept(event);
            }
        };
        return accept(handler);
    }

    public WebSocketEvent<T> acceptCloseEventConsumer(Consumer<CloseEvent> consumer) {
        WebSocketEventHandler<T> handler = new WebSocketEventHandler<T>() {
            @Override
            public void handle(CloseEvent event) {
                consumer.accept(event);
            }
        };
        return accept(handler);
    }

    public abstract WebSocketEvent<T> accept(WebSocketEventHandler<T> handler);

    public WebSocketTextEndpoint endPoint() {
        return endpoint;
    }

    public <E extends WebSocketEvent<U>,U> E copy(Function<T, U> f) {
        return (E) this;
    }

}


