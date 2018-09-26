package automat;

import automat.messages.HeartbeatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public abstract class WebSocketMessage {

    private static final Logger logger = LogManager.getLogger(WebSocketMessage.class);

    private WebSocketMessageType type;
    protected String payload;

    public WebSocketMessage() {
    }

    public WebSocketMessage(WebSocketMessageType type) {
        this.setMsgType(type);
    }

    public abstract String toJson();

    @Override
    public int hashCode() {
        return type.hashCode() ^ payload.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(obj == this) {
            return true;
        }
        if(obj instanceof WebSocketMessage) {
            WebSocketMessage other = (WebSocketMessage) obj;
            return other.type.equals(type) && other.payload.equals(payload);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getTypeName()+"["+type+", "+payload+"]";
    }

    protected <T> String envelope(T payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(type.apply(payload));
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static WebSocketMessage from(String jsonString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readValue(jsonString, JsonNode.class);
            String msgTypeString = jsonNode.get("msgType").asText();
            JsonNode payload = jsonNode.get("payload");
            WebSocketMessageType msgType = WebSocketMessageType.valueOf(msgTypeString.toUpperCase());
            Class<? extends WebSocketMessage> clazz = msgType.newInstance().getClass();
            return mapper.readValue(jsonString, (Class<? extends WebSocketMessage>) clazz);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setMsgType(WebSocketMessageType type) {
        this.type = type;
    }

    public void setMsgType(String type) {
        setMsgType(WebSocketMessageType.valueOf(type.toUpperCase()));
    }

    public enum WebSocketMessageType {

        HEARTBEAT("heartbeat", () -> new HeartbeatMessage());

        private String msgType;
        private Supplier<? extends WebSocketMessage> supplier;


        <E extends WebSocketMessage, T> WebSocketMessageType(String msgType, Supplier<? extends WebSocketMessage> supplier) {
            this.msgType = msgType;
            this.supplier = supplier;
        }

        public <T> Map<String,?> apply(T payload) {
            return MapBuilder.key("msgType").value(msgType).key("payload").value(payload).build();
        }

        public WebSocketMessage newInstance() {
            return supplier.get();
        }
    }
}

