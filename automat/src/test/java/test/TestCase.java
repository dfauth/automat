package test;

import automat.WebSocketMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

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

        BlockingQueue<WebSocketMessage> queue = new ArrayBlockingQueue<>(100);

        given().environment(LOCAL).
                identity(WATCHERBGYPSY).
                onRequest().
                apply(authHandler).
                onResponse().
                apply(
                  forHttpCode(403).
                  use(
                    loginHandler(AUTH)
                    .andThen(storeToken)
                    .andThen(subscribeTo(
                      SUBSCRIPTION,
                      heartbeatConsumer
                    ) // subscribeTo
                    ) // andThen
                  ) // use
                ).

        get(IDENTITY).then().
                statusCode(200).
                body("users[0].username", is(WATCHERBGYPSY.username()));

        WebSocketMessage message = null;
        int cnt = 0;
        do {
            message = queue.poll(600, TimeUnit.SECONDS);
            cnt++;
            logger.info("received message: "+message);
        } while(message != null && cnt < 10);
        Assert.assertNotNull(message);

    }

}
