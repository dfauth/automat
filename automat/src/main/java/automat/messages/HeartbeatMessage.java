package automat.messages;

import automat.WebSocketMessage;

public class HeartbeatMessage extends WebSocketMessage<String> {


    public HeartbeatMessage() {
    }

    public HeartbeatMessage(String payload) {
        super(WebSocketMessage.WebSocketMessageType.HEARTBEAT);
        setPayload(payload);
    }

    @Override
    public String toJson() {
        return envelope(payload);
    }

}
