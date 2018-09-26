package automat;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static automat.Automat.given;

public class Functions {

    private static final Logger logger = LogManager.getLogger(Functions.class);

    private static ExecutorService executor = Executors.newCachedThreadPool();

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

    public static <T,U> Consumer<? extends WebSocketEvent<T>> delay(Supplier<Long> duration, BiConsumer<? extends WebSocketEvent<T>,DelayBehaviour> consumer) {
        return delay(duration, consumer, Function.identity());
    }

    public static <T,U> Consumer<? extends WebSocketEvent<T>> delay(Supplier<Long> duration, BiConsumer<? extends WebSocketEvent<U>,DelayBehaviour> consumer, Function<T, U> f) {
        return e -> executor.submit(()-> consumer.accept(e.copy(f), new DelayBehaviour(duration)));
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

    public static <T,U> Consumer<T> filter(Predicate<T> predicate, Consumer<T> after) {
        return t -> {
            if(predicate.test(t)) {
                after.accept(t);
            }
        };
    }

    public static <T,U> Function<T, Optional<U>> filter(Predicate<T> predicate, Function<T,U> after) {
        return t -> predicate.test(t)?
                Optional.of(after.apply(t)) :
                Optional.empty();
    }

    public static <T> Consumer<T> despatch(Consumer<T> consumer) {
        return t -> executor.submit(()-> consumer.accept(t));
    }

    public static <T,U> Function<T,Future<U>> despatch(Function<T,U> function) {
        return t -> executor.submit(()-> function.apply(t));
    }

    public static <T> Consumer<T> split(Consumer<T>... consumers) {
        return t -> Stream.of(consumers).forEach(c -> despatch(c).accept(t));
    }

    public static <T,U> Function<T,Set<Future<U>>> split(Function<T,U>... functions) {
        return t -> Stream.of(functions).map(f -> despatch(f).apply(t)).collect(Collectors.toSet());
    }

    public static <T> Consumer<T> switchIt(Predicate<T> predicate, Consumer<T> ifTrue, Consumer<T> ifFalse) {
        return t -> {
            if(predicate.test(t)) {
                ifTrue.accept(t);
            } else {
                ifFalse.accept(t);
            }
        };
    }

    public static <T,U> Function<T,U> switchIt(Predicate<T> predicate, Function<T,U> ifTrue, Function<T,U> ifFalse) {
        return t -> {
            if(predicate.test(t)) {
                return ifTrue.apply(t);
            } else {
                return ifFalse.apply(t);
            }
        };
    }
}
