package automat.messages;

import static automat.WebSocketMessage.WebSocketMessageType.UNKNOWN;


public class UnknownMessage extends ApplicationMessage {

    private String msgTypeString;

    public UnknownMessage() {
        super(UNKNOWN);
    }

    public UnknownMessage(String msgTypeString, String payload) {
        super(UNKNOWN);
        this.msgTypeString = msgTypeString;
        this.payload = payload;
    }
}
