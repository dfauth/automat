import automat.{Functions, Identity}
import automat.Automat.given
import org.apache.logging.log4j.scala.Logging
import org.hamcrest.Matchers.is
import org.scalatest._
import TestCase.JSON_STRING
import automat.Identity.WATCHERBGYPSY
import automat.Functions.{authHandler, loginHandler}
import automat.Automat.Utils.forHttpCode

class TestSpec extends FlatSpec with Matchers with Logging {

  "create an account" should "work" in {
      given.

      when.port(9000).body(JSON_STRING).post("/api/client/registration").

    then.statusCode(200)
    }

  "Json serialization" should "work" in {

    given.identity(WATCHERBGYPSY).
      onRequest().apply(authHandler).onResponse().apply(forHttpCode(403).use(loginHandler)).

    when.get("/api/state/identity").


    then().
        statusCode(200).
      body("users[0].username", is(WATCHERBGYPSY.getUsername))
  }

}


