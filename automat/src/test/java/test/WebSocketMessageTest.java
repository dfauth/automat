package test;

import automat.WebSocketMessage;
import automat.messages.HeartbeatMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;


public class WebSocketMessageTest {

    private static final Logger logger = LogManager.getLogger(WebSocketMessageTest.class);
    private static final String HEARTBEAT_REF = "{\"msgType\":\"heartbeat\",\"payload\":\"ping\"}";

    @Test
    public void testIt() {
        Assert.assertEquals(new HeartbeatMessage("ping").toJson(), HEARTBEAT_REF);
        Assert.assertEquals(WebSocketMessage.from(HEARTBEAT_REF), new HeartbeatMessage("ping"));

    }
}
