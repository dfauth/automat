package test;

import org.testng.annotations.Test;

import static automat.Automat.Utils.forHttpCode;
import static automat.Automat.given;
import static automat.Environment.LOCAL;
import static automat.Functions.authHandler;
import static automat.Functions.loginHandler;
import static test.TestIdentity.WATCHERBGYPSY;
import static test.TestResource.IDENTITY;
import static test.TestResource.REGISTRATION;
import static org.hamcrest.Matchers.is;

public class TestCase {


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

                then().statusCode(400);
    }

    @Test(groups = "identity")
    public void testIdentity() {

        given().environment(LOCAL).
                identity(WATCHERBGYPSY).
                onRequest().apply(authHandler).
                onResponse().apply(
                forHttpCode(403).use(loginHandler) //.andThen(storeToken)
        ).
        get(IDENTITY).then().
                statusCode(200).
                body("users[0].username", is(WATCHERBGYPSY.username()));

    }
}
