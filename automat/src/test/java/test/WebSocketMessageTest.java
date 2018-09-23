package test;

import automat.WebSocketMessage;
import automat.messages.HeartbeatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;


public class WebSocketMessageTest {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketMessageTest.class);
    private static final String HEARTBEAT_REF = "{\"msgType\":\"heartbeat\",\"payload\":\"ping\"}";

    @Test
    public void testIt() {
        Assert.assertEquals(new HeartbeatMessage("ping").toJson(), HEARTBEAT_REF);
        Assert.assertEquals(WebSocketMessage.from(HEARTBEAT_REF), new HeartbeatMessage("ping"));

    }
}
