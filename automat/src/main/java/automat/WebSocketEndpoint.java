package automat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.*;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


@ClientEndpoint
public class WebSocketEndpoint {

    private static final Logger logger = LogManager.getLogger(WebSocketEndpoint.class);

    private final Consumer<Session> _onOpen;
    private final BiConsumer<String, Session> _onText;
    private final BiConsumer<CloseReason, Session> _onClose;
    private Session session;

    public WebSocketEndpoint() {
        this((t,s)->{});
    }

    public WebSocketEndpoint(BiConsumer<String, Session> onText) {
        this(s->{}, onText, (r,s)->{});
    }

    public WebSocketEndpoint(Consumer<Session> onOpen, BiConsumer<String, Session> onText, BiConsumer<CloseReason, Session> onClose) {
        _onOpen = onOpen;
        _onText = onText;
        _onClose = onClose;
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
}
