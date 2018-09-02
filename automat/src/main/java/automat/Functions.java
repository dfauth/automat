package automat;

import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Functions {

    private static final Logger logger = LogManager.getLogger(Functions.class);

    public static Function<Automat, UnaryOperator<FilterableRequestSpecification>> authHandler = ctx -> r -> {
        ctx.authToken().<Void>map(t -> {
            r.header("Authorization", "Bearer "+t);
            return null;
        });
        return r;
    };

    public static Function<Automat, Function<Response, Response>> storeToken  = ctx -> r -> {
        ctx.authToken(r.body().jsonPath().getString("authToken"));
        ctx.refreshToken(r.body().jsonPath().getString("refreshToken"));
        return r;
    };

    public static Function<Automat, Function<FilterableRequestSpecification, Response>> loginHandler  = ctx -> {
        String jsonString = "{\n\t\"username\": \"" + ctx.identity().map(i -> i.username()).orElse(null) + "\",\n\t\"password\": \"" + ctx.identity().map(i -> i.getPassword()).orElse(null) + "\"\n}";
        return ((Function<FilterableRequestSpecification, Response>) r -> {
            logger.info("fire loginHandler: "+jsonString);
            Response res = r.body(jsonString).post("http://localhost/api/user/login");
            logger.info("res: "+res.statusCode());
            r.then().statusCode(200);
            return res;
        }).andThen(storeToken.apply(ctx));
    };
//        return r -> {
//            logger.info("fire loginHandler: "+jsonString);
//            Response res = r.body(jsonString).post("http://localhost/api/user/login");
//            logger.info("res: "+res.statusCode());
//            r.then().statusCode(200);
//            return res;
//        }.andThen(storeToken.apply(ctx));

}
