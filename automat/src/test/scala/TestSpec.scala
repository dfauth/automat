import automat.Thingy
import io.restassured.RestAssured.{given, when}
import io.restassured.response.{Response, ResponseOptions}
import io.restassured.specification.{RequestSpecification, ResponseSpecification}
import org.apache.logging.log4j.scala.Logging
import org.scalatest._
import thingy.{Given, RequestContext, ThingyFilter}

class TestSpec extends FlatSpec with Matchers with Logging {

  "create an account" should "work" in {
    val jsonString = "{\n    \"company\": \"Digital Cat\",\n    \"firstName\": \"Damir\",\n    \"lastName\": \"Palinic\",\n    \"email\": \"damir@palinic.com\",\n    \"username\": \"dpalinic\",\n    \"password\": \"test12345\"\n }"
      given.

      when.port(9000).body(jsonString).post("/api/client/registration").

    then.statusCode(200)
    }

  def storeToken(): Response => Response = {
    r => {
      RequestContext.addToContext("authToken", r.body().jsonPath().getString("authToken"))
      RequestContext.addToContext("refreshToken", r.body().jsonPath().getString("refreshToken"))
      r
    }
  }

  "Json serialization" should "work" in {
      val g = given.filter(new ThingyFilter(Map(403 -> loginHandler().andThen(storeToken()))))

      val res = g.when.port(9000).get("/api/state/identity")
//        get("http://localhost:9000/api/state/identity").

      val res1 = res.then()
      res1.statusCode(200)
    }

  def loginHandler():RequestSpecification => Response = {
    /**
      * {
	"username": "dpalinic",
	"password": "test12345"
}
      */
    val jsonString = "{\n\t\"username\": \"dpalinic\",\n\t\"password\": \"test12345\"\n}"
      req => {
        logger.info("fire loginHandler: "+jsonString)
        val res = req.body(jsonString).post("http://localhost/api/user/login")
        logger.info("res: "+res.statusCode())
        res
      }

  }

      "Again it" should "work" in {
        Given.given().handleStatusCode(403, loginHandler()).

          when.port(9000).get("/api/state/identity").

          then().
          statusCode(401)
      }
}


