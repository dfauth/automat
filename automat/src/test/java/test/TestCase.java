package test;

import automat.AutomationContext;
import automat.SubscriptionFilter;
import automat.WebSocketMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static automat.Automat.given;
import static automat.Environment.LOCAL;
import static automat.WebSocketMessage.WebSocketMessageType.KNOWN;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static test.TestIdentity.WATCHERBGYPSY;
import static test.TestResource.IDENTITY;
import static test.TestResource.REGISTRATION;
import static test.Configurations.basicClientWithWebSocket;

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
    public void testIdentity() throws InterruptedException, ExecutionException {

        AutomationContext ctx = given();

        ctx.environment(LOCAL).
                identity(WATCHERBGYPSY).
                configureAs(basicClientWithWebSocket).

        get(IDENTITY).then().
                statusCode(200).
                body("users[0].username", is(WATCHERBGYPSY.username()));

        CompletableFuture<WebSocketMessage> future = ctx.subscribe(new SubscriptionFilter(KNOWN), m -> {
            logger.info("received message: " + m);
        });
        Assert.assertNotNull(future.get());
    }

}
