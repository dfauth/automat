package automat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;


public abstract class WebSocketEndpoint<T> extends Endpoint implements MessageHandler.Partial<T> {

    private static final Logger logger = LogManager.getLogger(WebSocketEndpoint.class);

    private Consumer<WebSocketEvent<T>> consumer = e -> {};
    private final AutomationContext ctx;
    private final ClientEndpointConfig.Builder builder;
    private final URI uri;
    protected Session session;

    public WebSocketEndpoint(AutomationContext ctx, URI uri) {
        this.ctx = ctx;
        this.uri = uri;
        this.builder = ClientEndpointConfig.Builder.create();
        this.builder.configurator(new Configurator(ctx));
    }

    public void onEvent(Consumer<WebSocketEvent<T>> consumer) {
        this.consumer = consumer;
    }

    public <U> void onEvent(Consumer<U> consumer, Function<WebSocketEvent<T>,U> f) {
        this.consumer = t -> consumer.accept(f.apply(t));
    }

    public void sendMessage(T t) {
        sendMessage(t, Function.identity());
    }

    public <U> void sendMessage(U u, Function<U, T> f) {
        try {
            logger.info("WebSocketEndpoint: sendMessage: "+u);
            _sendMessage(f.apply(u));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected abstract void _sendMessage(T t) throws IOException;

    public ClientEndpointConfig getConfig() {
        return this.builder.build();
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        session.addMessageHandler(this);
        logger.info("WebSocketEndpoint: Connected to server. session: "+session);
        consumer.accept(new WebSocketEvent.OpenEvent(this));
    }

    @Override
    public void onClose(Session session, CloseReason reason) {
        logger.info("WebSocketEndpoint: Closing a WebSocket due to " + reason.getReasonPhrase());
        consumer.accept(new WebSocketEvent.CloseEvent(this, reason));
    }

    public void start() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, getConfig(), uri);
        } catch (DeploymentException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void onMessage(T message) {
        consumer.accept(createWebSocketMessageEvent(message));
    }

    protected abstract WebSocketEvent createWebSocketMessageEvent(T message);

    @Override
    public void onMessage(T partialMessage, boolean last) {
        appendBuffer(partialMessage);
        if(last) {
            onMessage(emptyBuffer());
        }
    }

    protected abstract T emptyBuffer();

    protected abstract void appendBuffer(T partialMessage);

    public static class Configurator extends ClientEndpointConfig.Configurator {

        private final AutomationContext ctx;

        public Configurator(AutomationContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void beforeRequest(Map<String, List<String>> headers) {
            super.beforeRequest(headers);
            ctx.authToken().<Void>map(t -> {
                headers.put("Authorization", Collections.singletonList("Bearer "+t));
                return null;
            });
        }
    }

}
