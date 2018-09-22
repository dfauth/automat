package automat;

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
    protected WebSocketEvent createWebSocketMessageEvent(String message) {
        return new WebSocketEvent.MessageEvent<>(this, message);
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
