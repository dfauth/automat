package thingy


import automat.Automat.Utils.forHttpCode
import automat.Automat.given
import automat.Functions.{authHandler, loginHandler}
import test.TestResource.{IDENTITY, REGISTRATION}
import org.apache.logging.log4j.scala.Logging
import org.hamcrest.Matchers.{is, isOneOf}
import org.scalatest.{FlatSpec, Matchers}
import test.TestIdentity.WATCHERBGYPSY
import test.User

class TestSpec extends FlatSpec with Matchers with Logging {

  "create an account" should "work" in {

    val user = new User().company("Thingy, Inc.").firstname("Watcher").lastname("BGypsy").email("watcherbgypsy@gmail.com").username(WATCHERBGYPSY.username).password(WATCHERBGYPSY.password)
    given.

      post(REGISTRATION, user).

    then.
      statusCode(isOneOf[Integer](200, 400))
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


