package automat;

import automat.events.CloseEvent;
import automat.events.MessageEvent;
import automat.events.OpenEvent;

import java.util.function.Consumer;

public abstract class WebSocketEvent {

    private final WebSocketTextEndpoint endpoint;

    public WebSocketEvent(WebSocketTextEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public <E extends WebSocketEvent> E acceptOpenEventConsumer(Consumer<OpenEvent> consumer) {
        WebSocketEventHandler handler = new WebSocketEventHandler() {
            @Override
            public void handle(OpenEvent event) {
                consumer.accept(event);
            }
        };
        return accept(handler);
    }

    public <E extends WebSocketEvent,T> E acceptMessageEventConsumer(Consumer<MessageEvent<T>> consumer) {
        WebSocketEventHandler handler = new WebSocketEventHandler() {
            @Override
            public void handle(MessageEvent event) {
                consumer.accept(event);
            }
        };
        return accept(handler);
    }

    public <E extends WebSocketEvent> E acceptCloseEventConsumer(Consumer<CloseEvent> consumer) {
        WebSocketEventHandler handler = new WebSocketEventHandler() {
            @Override
            public void handle(CloseEvent event) {
                consumer.accept(event);
            }
        };
        return accept(handler);
    }

    public abstract <E extends WebSocketEvent> E accept(WebSocketEventHandler handler);

    public WebSocketTextEndpoint endPoint() {
        return endpoint;
    }

}


