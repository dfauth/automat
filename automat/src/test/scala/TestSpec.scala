import automat.Identity
import io.restassured.RestAssured.given
import org.apache.logging.log4j.scala.Logging
import org.hamcrest.Matchers.is
import org.scalatest._
import thingy.handlers.{asFilter, authHandler, loginHandler, storeToken}
import thingy.{TestContext, ThingyFilter}
import thingy.Given.use

class TestSpec extends FlatSpec with Matchers with Logging {

  "create an account" should "work" in {
    val jsonString = "{" +
      "    \"company\": \"Thingy\"," +
      "    \"firstName\": \"Watcher\"," +
      "    \"lastName\": \"BGypsy\"," +
      "    \"email\": \"watcherbgypsy@gmail.com\"," +
      "    \"username\": \"watcherbgypsy\"," +
      "    \"password\": \"password\"" +
      " }"
      given.

      when.port(9000).body(jsonString).post("/api/client/registration").

    then.statusCode(200)
    }

  "Json serialization" should "work" in {
    val ctx = use(Identity.WATCHERBGYPSY)
      given.
        filter(asFilter(
          preHandler = authHandler(ctx),
          postHandlers = Map(
            403 -> loginHandler(ctx).andThen(storeToken(ctx))
          )
        )).

      when.
        port(9000).get("/api/state/identity").

      then().
        statusCode(200).
        body("users[0].username", is(ctx.get("username").getOrElse(throw new IllegalArgumentException("no test property 'username' found"))))
    }

}


