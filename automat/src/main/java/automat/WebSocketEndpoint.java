package automat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


@ClientEndpoint()
//@ClientEndpoint(configurator = WebSocketEndpoint.Configurator.class)
public class WebSocketEndpoint extends Endpoint {

    private static final Logger logger = LogManager.getLogger(WebSocketEndpoint.class);

    private final Consumer<Session> _onOpen;
    private final BiConsumer<String, Session> _onText;
    private final BiConsumer<CloseReason, Session> _onClose;
    private final AutomationContext ctx;
    private final ClientEndpointConfig.Builder builder;
    private Session session;

    public WebSocketEndpoint(AutomationContext ctx) {
        this(ctx, (t,s)->{});
    }

    public WebSocketEndpoint(AutomationContext ctx, BiConsumer<String, Session> onText) {
        this(ctx, s->{}, onText, (r,s)->{});
    }

    public WebSocketEndpoint(AutomationContext ctx, Consumer<Session> onOpen, BiConsumer<String, Session> onText, BiConsumer<CloseReason, Session> onClose) {
        this.ctx = ctx;
        _onOpen = onOpen;
        _onText = onText;
        _onClose = onClose;
        this.builder = ClientEndpointConfig.Builder.create();
        this.builder.configurator(new Configurator(ctx));
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        logger.info("WebSocketEndpoint: Connected to server. session: "+session);
        _onOpen.accept(session);
    }

    @OnMessage
    public void onText(String message, Session session) {
        _onText.accept(message,session);
    }

    @OnClose
    public void onClose(CloseReason reason, Session session) {
        logger.info("WebSocketEndpoint: Closing a WebSocket due to " + reason.getReasonPhrase());
        _onClose.accept(reason, session);
    }

    public void sendMessage(String str) {
        try {
            session.getBasicRemote().sendText(str);
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
        logger.info("WebSocketEndpoint: Connected to server. session: "+session);
        _onOpen.accept(session);
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
