package automat;

import automat.events.CloseEvent;
import automat.events.MessageEvent;
import automat.events.OpenEvent;

import javax.websocket.CloseReason;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;


public class WebSocketTextEndpoint extends WebSocketEndpoint<String> {

    private StringWriter buffer = new StringWriter();

    public WebSocketTextEndpoint(AutomationContext ctx, URI uri) {
        super(ctx, uri);
    }

    @Override
    protected void _sendMessage(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    @Override
    protected WebSocketEvent createOpenEvent() {
        return new OpenEvent(this);
    }

    @Override
    protected WebSocketEvent createCloseEvent(CloseReason reason) {
        return new CloseEvent(this, reason);
    }

    @Override
    protected WebSocketEvent createMessageEvent(String message) {
        return new MessageEvent<>(this, message);
    }

    @Override
    protected String emptyBuffer() {
        try {
            return buffer.toString();
        } finally {
            buffer.getBuffer().setLength(0);
        }
    }

    @Override
    protected void appendBuffer(String partialMessage) {
        buffer.append(partialMessage);
    }

}
