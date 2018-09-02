import automat.{Functions, Identity}
import io.restassured.RestAssured.given
import org.apache.logging.log4j.scala.Logging
import org.hamcrest.Matchers.is
import org.scalatest._
import TestCase.JSON_STRING
import automat.Identity.WATCHERBGYPSY
import automat.RestClientContext.identity
import automat.Functions.{authHandler, loginHandler}
import automat.RestClientContext.Utils.forHttpCode

class TestSpec extends FlatSpec with Matchers with Logging {

  "create an account" should "work" in {
      given.

      when.port(9000).body(JSON_STRING).post("/api/client/registration").

    then.statusCode(200)
    }

  "Json serialization" should "work" in {

    val ctx = identity(WATCHERBGYPSY).
      onRequest().apply(authHandler).onResponse().apply(forHttpCode(403).use(loginHandler))

    given().filter(ctx.asFilter()).

    when.
        port(9000).get("/api/state/identity").


    then().
        statusCode(200).
      body("users[0].username", is(WATCHERBGYPSY.getUsername))
  }

}


