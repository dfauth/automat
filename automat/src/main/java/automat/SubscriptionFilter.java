package automat;

import java.util.Arrays;
import java.util.List;

public class SubscriptionFilter {

    public static final SubscriptionFilter ALL = new SubscriptionFilter(WebSocketMessage.WebSocketMessageType.values());

    private final List<WebSocketMessage.WebSocketMessageType> messageTypes;

    public SubscriptionFilter(WebSocketMessage.WebSocketMessageType... messageTypes) {
        this.messageTypes = Arrays.asList(messageTypes);
    }

    public boolean accept(WebSocketMessage message) {
        return this.messageTypes.contains(message.getMsgType());
    }
}
