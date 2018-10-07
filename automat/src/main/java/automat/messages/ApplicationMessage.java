package automat.messages;

import automat.WebSocketMessage;
import automat.WebSocketMessageHandler;

import java.util.Optional;

public class ApplicationMessage extends WebSocketMessage {


    public ApplicationMessage(WebSocketMessage.WebSocketMessageType type) {
        super(type);
    }

    public ApplicationMessage(WebSocketMessage.WebSocketMessageType type, String payload) {
        super(type);
        setPayload(payload);
    }

    @Override
    public boolean isApplicationMessage() {
        return true;
    }

    @Override
    public <T> Optional<T> accept(WebSocketMessageHandler<T> handler) {
        return handler.handle(this);
    }

}
