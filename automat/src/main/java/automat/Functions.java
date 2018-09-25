package automat;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

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

    public static Function<Response, Response> subscribeTo(Resource resource, Consumer<WebSocketEvent> consumer) {
        AutomationContext ctx = given();
        return r-> {
            if(r.statusCode() == 200) {
                logger.info("subscribing to "+resource);
                // subscribe
                WebSocketEndpoint endPoint = new WebSocketTextEndpoint(given(), ctx.toUri("ws", resource));
                endPoint.onEvent(consumer);
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

    public static <E extends WebSocketEvent> Consumer<E> delay(Supplier<Long> duration, BiConsumer<E,DelayBehaviour> consumer) {
        return e -> Executors.newSingleThreadExecutor().submit(()-> consumer.accept(e, new DelayBehaviour(duration)));
    }

    public static class DelayBehaviour {

        private final Supplier<Long> duration;

        public DelayBehaviour(Supplier<Long> duration) {
            this.duration = duration;
        }

        public void sleep() {
            try {
                Thread.sleep(duration.get());
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    public static Supplier<Long> seconds(int n) {
        return timeUnit(n, TimeUnit.SECONDS);
    }

    public static Supplier<Long> timeUnit(int n, TimeUnit unit) {
        return ()-> unit.toMillis(n);
    }
}
