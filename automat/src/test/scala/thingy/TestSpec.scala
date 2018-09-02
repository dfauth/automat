package thingy


import automat.Automat.Utils.forHttpCode
import automat.Automat.given
import automat.Functions.{authHandler, loginHandler}
import test.TestResource.{IDENTITY, REGISTRATION}
import org.apache.logging.log4j.scala.Logging
import org.hamcrest.Matchers.is
import org.scalatest.{FlatSpec, Matchers}
import test.TestCase.JSON_STRING
import test.TestIdentity.WATCHERBGYPSY

class TestSpec extends FlatSpec with Matchers with Logging {

  "create an account" should "work" in {
      given.

      post(REGISTRATION, JSON_STRING).

    then.statusCode(200)
    }

  "Json serialization" should "work" in {

    given.identity(WATCHERBGYPSY).
      onRequest().apply(authHandler).onResponse().apply(forHttpCode(403).use(loginHandler)).

    get(IDENTITY).


    then().
        statusCode(200).
      body("users[0].username", is(WATCHERBGYPSY.username))
  }

}


