import io.restassured.RestAssured;
import org.testng.annotations.*;

import static io.restassured.RestAssured.given;

public class TestCase {

    @Test
    public void testIt() {
        given().
                when().
                get("http://localhost:9000/api/state/identity").
                then().
                statusCode(403);

    }
}
