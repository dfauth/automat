package test;

import automat.WebSocketEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static automat.Automat.Utils.forHttpCode;
import static automat.Automat.given;
import static automat.Environment.LOCAL;
import static automat.Functions.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static test.TestIdentity.WATCHERBGYPSY;
import static test.TestResource.*;

public class TestCase {

    public static final Logger logger = LogManager.getLogger(TestCase.class);

    @Test(groups = "createUser")
    public void testCreateUser() {

        User user = new User().company("Thingy, Inc.").
                firstname("Watcher").
                lastname("BGypsy").
                email("watcherbgypsy@gmail.com").
                username(WATCHERBGYPSY.username()).
                password(WATCHERBGYPSY.password());

        given().

                post(REGISTRATION, user).

                then().statusCode(isOneOf(200,400));
    }

    @Test(groups = "identity")
    public void testIdentity() throws InterruptedException {

        BlockingQueue<String> queue = new ArrayBlockingQueue<>(100);

        given().environment(LOCAL).
                identity(WATCHERBGYPSY).
                onRequest().apply(authHandler).
                onResponse().apply(
                forHttpCode(403).use(loginHandler(AUTH).andThen(storeToken).andThen(subscribeTo(SUBSCRIPTION, e -> {
                    e.accept(new WebSocketEvent.WebSocketEventHandler<String>(){
                        @Override
                        public void handle(WebSocketEvent.OpenEvent<String> event) {
                            delay(5, TimeUnit.SECONDS, event, b->{
                                b.sleep();
                                b.event.endPoint().sendMessage("ping");
                            });
                        }
                    }).accept(new WebSocketEvent.WebSocketEventHandler<String>(){
                        @Override
                        public void handle(WebSocketEvent.MessageEvent<String> event) {
                            delay(5, TimeUnit.SECONDS, event, b->{
                                logger.info("received: "+b.event.getMessage());
                                queue.offer(event.getMessage());
                                b.sleep();
                                e.endPoint().sendMessage("ping");
                            });
                        }
                    });
                })))).

        get(IDENTITY).then().
                statusCode(200).
                body("users[0].username", is(WATCHERBGYPSY.username()));

        String message = null;
        int cnt = 0;
        do {
            message = queue.poll(10, TimeUnit.SECONDS);
            cnt++;
            logger.info("received message: "+message);
        } while(message != null && cnt < 10);
        Assert.assertNotNull(message);

    }

    private <E extends WebSocketEvent<T>,T> void delay(int period, TimeUnit unit, E event, Consumer<DelayBehaviour<E,T>> consumer) {
        Executors.newSingleThreadExecutor().submit(()-> consumer.accept(new DelayBehaviour(period, unit, event)));
    }

    public static class DelayBehaviour<E extends WebSocketEvent<T>,T> {
        private final E event;
        private final int period;
        private final TimeUnit unit;

        public DelayBehaviour(int period, TimeUnit unit, E event) {
            this.period = period;
            this.unit = unit;
            this.event = event;
        }

        void sleep() {
            try {
                Thread.sleep(unit.toMillis(period));
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }
}
