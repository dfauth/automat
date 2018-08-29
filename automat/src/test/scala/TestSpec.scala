import io.restassured.RestAssured.{given, when}
import org.apache.logging.log4j.scala.Logging
import org.scalatest._

class TestSpec extends FlatSpec with Matchers with Logging {

    "Json serialization" should "work" in {
      given.

      when.
        get("http://localhost:9000/api/state/identity").

      then().
        statusCode(403)
    }
}


