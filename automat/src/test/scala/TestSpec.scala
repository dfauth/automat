import io.restassured.RestAssured.given
import org.apache.logging.log4j.scala.Logging
import org.scalatest._

class TestSpec extends FlatSpec with Matchers with Logging {

    "Json serialization" should "work" in {
      logger.info("in the test")
      val response = given().port(9000).get("http://localhost:9000/api/state/identity")
      logger.info("response: "+response)
      response.statusCode() should be (403)
    }
}


