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

    public static final String JSON_STRING = "{" +
            "    \"company\": \"Thingy\"," +
            "    \"firstName\": \"Watcher\"," +
            "    \"lastName\": \"BGypsy\"," +
            "    \"email\": \"watcherbgypsy@gmail.com\"," +
            "    \"username\": \"watcherbgypsy\"," +
            "    \"password\": \"password\"" +
            " }";

    @Test(groups = "createUser")
    public void testCreateUser() {
        given().

                post(REGISTRATION, JSON_STRING).

                then().statusCode(200);
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
