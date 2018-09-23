package automat;

import javax.websocket.CloseReason;

public abstract class WebSocketEvent<T> {

    private final WebSocketEndpoint<T> endpoint;

    public WebSocketEvent(WebSocketEndpoint<T> endpoint) {
        this.endpoint = endpoint;
    }

    public abstract WebSocketEvent<T> accept(WebSocketEventHandler<T> handler);

    public WebSocketEndpoint<T> endPoint() {
        return endpoint;
    }

    public static abstract class WebSocketEventHandler<T> {
        public void handle(OpenEvent<T> event) {}

        public void handle(MessageEvent<T> event) {}

        public void handle(CloseEvent<T> event) {}

    }

    public static class OpenEvent<T> extends WebSocketEvent<T> {

        public OpenEvent(WebSocketEndpoint<T> endpoint) {
            super(endpoint);
        }

        @Override
        public WebSocketEvent<T> accept(WebSocketEventHandler<T> handler) {
            handler.handle(this);
            return this;
        }
    }

    public static class MessageEvent<T> extends WebSocketEvent<T> {
        private T message;

        public MessageEvent(WebSocketEndpoint<T> endpoint, T message) {
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
    }

    public static class CloseEvent<T> extends WebSocketEvent<T> {
        private final CloseReason reason;

        public CloseEvent(WebSocketEndpoint<T> endpoint, CloseReason reason) {
            super(endpoint);
            this.reason = reason;
        }

        @Override
        public WebSocketEvent<T> accept(WebSocketEventHandler<T> handler) {
            handler.handle(this);
            return this;
        }
    }
}


