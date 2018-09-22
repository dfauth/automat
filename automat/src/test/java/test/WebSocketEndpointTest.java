package test;

import automat.WebSocketEndpoint;
import automat.WebSocketEvent;
import automat.WebSocketTextEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static automat.Automat.given;
import static java.lang.Thread.sleep;


public class WebSocketEndpointTest {

    private static final Logger logger = LogManager.getLogger(WebSocketEndpointTest.class);

    @Test
    public void testIt() throws URISyntaxException, InterruptedException {
        WebSocketEndpoint endpoint = new WebSocketTextEndpoint(given(),new URI("ws://localhost:9000/stream"));
        endpoint.onEvent(onOpen());
        endpoint.start();
        sleep(100000);
    }

    private Consumer<String> onText(WebSocketEndpoint endpoint) {
        return t -> {
            Executors.newSingleThreadExecutor().submit(()->{
                try {
                    logger.info("received message: "+t);
                    sleep(5000);
                    endpoint.sendMessage("ping");
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            });
        };
    }

    private Consumer<WebSocketEvent<String>> onOpen() {
        return event -> {
            Executors.newSingleThreadExecutor().submit(()->{
                try {
                    logger.info("session open");
                    sleep(5000);
                    event.endPoint().sendMessage("ping");
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            });
        };
    }
}
