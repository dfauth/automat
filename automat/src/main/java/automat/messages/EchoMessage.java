package automat.messages;

public class EchoMessage extends ApplicationMessage {

    public EchoMessage() {
        super(WebSocketMessageType.ECHO);
    }

    public EchoMessage(String payload) {
        super(WebSocketMessageType.ECHO, payload);
    }
}
