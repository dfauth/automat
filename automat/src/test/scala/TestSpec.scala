import io.restassured.RestAssured.given
import org.apache.logging.log4j.scala.Logging
import org.hamcrest.Matchers.is
import org.scalatest._
import thingy.handlers.{asFilter, authHandler, loginHandler, storeToken}
import thingy.{RequestContext, ThingyFilter}

class TestSpec extends FlatSpec with Matchers with Logging {

  "create an account" should "work" in {
    val jsonString = "{" +
      "    \"company\": \"Digital Cat\"," +
      "    \"firstName\": \"Damir\"," +
      "    \"lastName\": \"Palinic\"," +
      "    \"email\": \"damir@palinic.com\"," +
      "    \"username\": \"dpalinic\"," +
      "    \"password\": \"test12345\"" +
      " }"
      given.

      when.port(9000).body(jsonString).post("/api/client/registration").

    then.statusCode(200)
    }

  "Json serialization" should "work" in {
      given.
        filter(asFilter(
          preHandler = authHandler(RequestContext),
          postHandlers = Map(
            403 -> loginHandler().andThen(storeToken())
          )
        )).

      when.
        port(9000).get("/api/state/identity").

      then().
        statusCode(200).
        body("users[0].username", is("dpalinic"))
    }

}


