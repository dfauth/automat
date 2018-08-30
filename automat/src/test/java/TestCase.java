public class TestCase {

    @Test
    public void testIt() {
        given.

                when.
                get("http://localhost:9000/api/state/identity").

                then().
                statusCode(403)

    }
}
