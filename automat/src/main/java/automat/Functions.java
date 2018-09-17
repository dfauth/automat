package automat;

import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Functions {

    private static final Logger logger = LogManager.getLogger(Functions.class);

    public static UnaryOperator<FilterableRequestSpecification> authHandler = r -> {
        Automat ctx = Automat.given();
        ctx.authToken().<Void>map(t -> {
            r.header("Authorization", "Bearer "+t);
            return null;
        });
        return r;
    };

    public static Function<Response, Response> subscribeTo(Resource resource) {
        Automat ctx = Automat.given();
        return r-> {
            if(r.statusCode() == 200) {
                logger.info("subscribing to "+resource);
                // subscribe
            }
            return r;
        };
    };

    public static Function<Response, Response> storeToken  = r -> {
        Automat ctx = Automat.given();
        ctx.authToken(r.body().jsonPath().getString("authToken"));
        ctx.refreshToken(r.body().jsonPath().getString("refreshToken"));
        return r;
    };

    public static Function<FilterableRequestSpecification, Response> loginHandler  = r -> {
        Automat ctx = Automat.given();
        String jsonString = "{\n\t\"username\": \"" + ctx.identity().map(i -> i.username()).orElse(null) + "\",\n\t\"password\": \"" + ctx.identity().map(i -> i.password()).orElse(null) + "\"\n}";
        logger.info("fire loginHandler: "+jsonString);
        Response res = r.body(jsonString).post("http://localhost/api/user/login");
        logger.info("res: "+res.statusCode());
        r.then().statusCode(200);
        return res;
    };
}
