package automat;

import automat.events.MessageEvent;
import automat.events.OpenEvent;
import automat.messages.HeartbeatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static automat.Automat.automationContext;
import static automat.Automat.given;

public class Functions {

    private static final Logger logger = LogManager.getLogger(Functions.class);

    private static ExecutorService executor = Executors.newCachedThreadPool();

    public static UnaryOperator<FilterableRequestSpecification> authHandler = r -> {
        AutomationContext ctx = given();
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
                WebSocketEndpoint endPoint = ctx.webSocketEndpoint(resource);
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

    public static Function<RequestSpecification, Response> loginHandler(Resource resource) {
        AutomationContext ctx = given();
        return r -> {
            RequestSpecification tmp = r.contentType(ContentType.JSON).body(IdentityBean.of(ctx.identity()));
            Response res = tmp.log().all().post(ctx.toUri(resource));
            res.then()
                    .statusCode(200)
                    .body("authToken", Matchers.notNullValue())
                    .body("refreshToken", Matchers.notNullValue());
            return res;
        };
    }

    public static Consumer<WebSocketEvent> heartbeat = e -> e.endPoint().sendMessage(new HeartbeatMessage("ping").toJson());

    public static Function<WebSocketEvent,HeartbeatMessage> heartbeatResponse = e -> new HeartbeatMessage("ping");

    public static Consumer<MessageEvent<HeartbeatMessage>> heartbeatResponseConsumer = heartbeatResponseConsumer(automationContext().heartbeatInterval());

    public static Consumer<MessageEvent<HeartbeatMessage>> heartbeatResponseConsumer(Duration duration) {
        return e -> delay(duration,e).thenAccept(heartbeat);
    }

    public static Consumer<OpenEvent> connectionConsumer = connectionConsumer(automationContext().heartbeatInterval());

    public static Consumer<OpenEvent> connectionConsumer(Duration delay) {
        return e -> delay(delay ,e).thenAccept(heartbeat);
    }

    public static Function<String, WebSocketMessage> messageTransformer = t -> WebSocketMessage.from(t);

    public static Function<MessageEvent<String>, MessageEvent<WebSocketMessage>> messageEventTransformer = e -> e.copy(messageTransformer);

    public static Consumer<MessageEvent<WebSocketMessage>> applicationMessageConsumer(BlockingQueue<WebSocketMessage> queue) {
        return e -> {
            logger.info("application message received: "+e.getMessage());
            queue.offer(e.getMessage());
        };
    }

    public static Consumer<MessageEvent<WebSocketMessage>> heartbeatMessageConsumer = heartbeatMessageConsumer(automationContext().heartbeatInterval());

    public static Consumer<MessageEvent<WebSocketMessage>> heartbeatMessageConsumer(Duration delay) {
        HeartbeatContext ctx = automationContext().heartbeatContext();
        return e -> {
            ctx.heartbeat();
            CompletionStage<HeartbeatMessage> future = delay(delay, e).thenApply(heartbeatResponse);
            future.thenAccept(r -> e.endPoint().sendMessage(r.toJson()));
        };
    }

    public static Consumer<MessageEvent<WebSocketMessage>> heartbeatFilter(BlockingQueue<WebSocketMessage> queue) {
        return filter((MessageEvent<WebSocketMessage> e) -> e.getMessage().isApplicationMessage(), applicationMessageConsumer(queue), heartbeatMessageConsumer);
    }

    public static Consumer<MessageEvent<String>> messageConsumer(BlockingQueue<WebSocketMessage> queue) {
        return e -> {
            logger.info("received message event with payload: "+e.getMessage());
            heartbeatFilter(queue).accept(messageEventTransformer.apply(e));
        };
    }

    public static Consumer<WebSocketEvent<String>> heartbeatConsumer(BlockingQueue<WebSocketMessage> queue) {
        return despatch(e -> {
            logger.info("received event: "+e);
            e.acceptOpenEventConsumer(connectionConsumer).acceptMessageEventConsumer(messageConsumer(queue));
        });
    }

    public static Duration seconds(int n) {
        return chronoUnit(ChronoUnit.SECONDS).apply(n);
    }

    public static Duration milliseconds(int n) {
        return chronoUnit(ChronoUnit.MILLIS).apply(n);
    }

    public static Function<Integer, Duration> chronoUnit(ChronoUnit unit) {
        return n -> Duration.of(n, unit);
    }

    public static <T> CompletionStage<T> delay(Duration duration, T event) {
        CompletableFuture<T> future = new CompletableFuture<>();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                future.complete(event);
            }
        }, duration.toMillis());

        return future;
    }

    public static <T,U> Consumer<T> filter(Predicate<T> predicate, Consumer<T> after) {
        return adapter(filter(predicate, adapter(after)));
    }

    public static <T,U> Consumer<T> filter(Predicate<T> predicate, Consumer<T> then, Consumer<T> otherwise) {
        return adapter(filter(predicate, adapter(then), adapter(otherwise)));
    }

    public static <T,U> Function<T, Optional<U>> filter(Predicate<T> predicate, Function<T,U> then) {
        return filter(predicate, (Function<T, Optional<U>>) t -> Optional.of(then.apply(t)), t -> Optional.empty());
    }

    public static <T,U> Function<T, U> filter(Predicate<T> predicate, Function<T,U> then, Function<T,U> otherwise) {
        return t -> predicate.test(t)?
                then.apply(t) :
                otherwise.apply(t);
    }

    public static Future<?> despatch(Runnable runnable) {
        return executor.submit(runnable);
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

    public static <T,U> Consumer<T> adapter(Function<T,U> f) {
        return t -> f.apply(t);
    }

    public static <T> Function<T,Void> adapter(Consumer<T> c) {
        return t -> {
            c.accept(t);
            return null;
        };
    }

    public static Function<Response,InputStream> toStream = r -> r.asInputStream();

    public static <T> Function<Response, List<T>> asListOf(Class<T> cls) {
        return toStream.andThen(
                (InputStream s) -> {
            try {
                return Arrays.asList((T[])new ObjectMapper().readValue(s, toArrayClass(cls)));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    public static <T> Class<?> toArrayClass(Class<T> c) {
        try {
            return Class.forName("[L"+c.getName()+";");
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
