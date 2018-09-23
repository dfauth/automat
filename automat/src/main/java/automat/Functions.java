package automat;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static automat.Automat.given;

public class Functions {

    private static final Logger logger = LogManager.getLogger(Functions.class);

    public static UnaryOperator<FilterableRequestSpecification> authHandler = r -> {
        AutomationContext ctx = given();
        logger.info("authHandler");
        ctx.authToken().<Void>map(t -> {
            r.header("Authorization", "Bearer "+t);
            return null;
        });
        return r;
    };

    public static Function<Response, Response> subscribeTo(Resource resource, Consumer<WebSocketEvent<String>> consumer) {
        AutomationContext ctx = given();
        return r-> {
            if(r.statusCode() == 200) {
                logger.info("subscribing to "+resource);
                // subscribe
                WebSocketEndpoint endPoint = new WebSocketTextEndpoint(given(), ctx.toUri("ws", resource));
                endPoint.onEvent((Consumer<WebSocketEvent<String>>) e -> consumer.accept(e));
                endPoint.start();
            }
            return r;
        };
    }

    public static Function<Response, Response> storeToken  = r -> {
        logger.info("storeToken");
        AutomationContext ctx = given();
        ctx.authToken(r.body().jsonPath().getString("authToken"));
        ctx.refreshToken(r.body().jsonPath().getString("refreshToken"));
        return r;
    };

    public static Function<FilterableRequestSpecification, Response> loginHandler(Resource resource) {
        AutomationContext ctx = given();
        return r -> {
            RequestSpecification tmp = r.contentType(ContentType.JSON).body(IdentityBean.of(ctx.identity()));
            Response res = tmp.log().all().post(ctx.toUri(resource));
            logger.info("loginHandler response statusCode: "+res.statusCode());
            r.then().statusCode(200);
            return res;
        };
    }

    public static Runnable delay(int period, TimeUnit unit, Consumer<DelayBehaviour> consumer) {
        return () -> Executors.newSingleThreadExecutor().submit(()-> consumer.accept(new DelayBehaviour(period, unit)));
    }

    public static class DelayBehaviour {
        private final int period;
        private final TimeUnit unit;

        public DelayBehaviour(int period, TimeUnit unit) {
            this.period = period;
            this.unit = unit;
        }

        public void sleep() {
            try {
                Thread.sleep(unit.toMillis(period));
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }
}
