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


public class WebSocketEndpoint extends Endpoint implements MessageHandler.Whole<String> {

    private static final Logger logger = LogManager.getLogger(WebSocketEndpoint.class);

    private Consumer<Session> _onOpen = s -> {};
    private Consumer<String> _onText = t -> {};
    private Consumer<CloseReason> _onClose = r -> {};
    private final AutomationContext ctx;
    private final ClientEndpointConfig.Builder builder;
    private final URI uri;
    private Session session;

    public WebSocketEndpoint(AutomationContext ctx, URI uri) {
        this.ctx = ctx;
        this.uri = uri;
        this.builder = ClientEndpointConfig.Builder.create();
        this.builder.configurator(new Configurator(ctx));
    }

    public void onOpen(Consumer<Session> consumer) {
        _onOpen = consumer;
    }

    public void onText(Consumer<String> consumer) {
        _onText = consumer;
    }

    public void onClose(Consumer<CloseReason> consumer) {
        _onClose = consumer;
    }

    public void sendMessage(String text) {
        try {
            logger.info("WebSocketEndpoint: sendMessage: "+text);
            session.getBasicRemote().sendText(text);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public ClientEndpointConfig getConfig() {
        return this.builder.build();
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        session.addMessageHandler(this);
        logger.info("WebSocketEndpoint: Connected to server. session: "+session);
        _onOpen.accept(session);
    }

    @Override
    public void onClose(Session session, CloseReason reason) {
        logger.info("WebSocketEndpoint: Closing a WebSocket due to " + reason.getReasonPhrase());
        _onClose.accept(reason);
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

    @Override
    public void onMessage(String message) {
        _onText.accept(message);
    }

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
