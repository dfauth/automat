import automat.Environment;
import automat.Identity;
import automat.RestClientContext;
import org.testng.annotations.Test;

import static automat.Functions.authHandler;
import static automat.Functions.loginHandler;
import static automat.RestClientContext.Utils.forHttpCode;
import static automat.RestClientContext.environment;
import static io.restassured.RestAssured.given;
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

                when().port(9000).body(JSON_STRING).post("/api/client/registration").

                then().statusCode(200);
    }

    @Test(groups = "identity")
    public void testIdentity() {
        RestClientContext ctx = environment(Environment.LOCAL).
                                use(Identity.WATCHERBGYPSY).
                onRequest().apply(authHandler).
                onResponse().apply(
                        forHttpCode(403).use(loginHandler) //.andThen(storeToken)
        );

        given().
                filter(ctx.asFilter()).

        when().
                port(9000).get("/api/state/identity").

                then().
                statusCode(200).
                body("users[0].username", is(ctx.identity().map(i -> i.username()).orElseThrow(()-> new IllegalArgumentException("No username"))));
    }
}
