package automat.messages;

import static automat.WebSocketMessage.WebSocketMessageType.KNOWN;


public class KnownMessage extends ApplicationMessage {

    public KnownMessage() {
        super(KNOWN);
    }

    public KnownMessage(String payload) {
        super(KNOWN);
        this.payload = payload;
    }
}
