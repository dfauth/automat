package automat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static automat.Environment.DefaultEnvironment.getEnvironment;

public class Automat implements AutomationContext {

    private static final Logger logger = LogManager.getLogger(Automat.class);
    private static final ThreadLocal<Automat> automats = ThreadLocal.withInitial(() -> new Automat());

    private final RequestBuilder requestBuilder = new RequestBuilder(this);
    private final ResponseBuilder responseBuilder = new ResponseBuilder(this);
    private String authToken;
    private String refreshToken;
    private Optional<Identity> identity = Optional.empty();
    private Optional<Environment> environment = Optional.empty();
    private BlockingQueue<WebSocketMessage> queue = new ArrayBlockingQueue<>(100);
    private Duration heartbeatInterval = Duration.of(5, ChronoUnit.SECONDS);
    private HeartbeatContext heartbeatContext = new HeartbeatContext(heartbeatInterval);
    private WebSocketTextEndpoint webSocketEndpoint;
    private Consumer<RequestLogSpecification> requestLogger = r->{};
    private Consumer<ResponseLogSpecification> responseLogger = r->{};

    public static AutomationContext automationContext() {
        return given();
    }

    public static AutomationContext given() {
        return automats.get();
    }

    private Automat() {
    }

    public AutomationContext use(Environment environment) {
        this.environment = Optional.of(environment);
        Environment.DefaultEnvironment.setEnvironment(environment);
        return this;
    }

    @Override
    public AutomationContext withHeartbeatInterval(Duration heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
        return this;
    }

    public Duration heartbeatInterval() {
        return this.heartbeatInterval;
    }

    @Override
    public AutomationContext async(WebSocketMessage message) {
        webSocketEndpoint.sendMessage(message.toJson());
        return this;
    }

    @Override
    public WebSocketEndpoint webSocketEndpoint(Resource resource) {
        webSocketEndpoint = new WebSocketTextEndpoint(this, toUri("ws", resource));
        return webSocketEndpoint;
    }

    @Override
    public AutomationContext logInstructions(Consumer<RequestLogSpecification> requestConsumer, Consumer<ResponseLogSpecification> responseConsumer) {
        this.requestLogger = l -> {
            requestConsumer.accept(l);
            this.requestLogger = r->{}; // reset this consumer
        };
        this.responseLogger = l -> {
            responseConsumer.accept(l);
            this.responseLogger = r->{}; // reset this consumer
        };

        return this;
    }

    public AutomationContext environment(Environment environment) {
        return use(environment);
    }

    public Automat use(Identity identity) {
        this.identity = Optional.of(identity);
        return this;
    }

    public Automat identity(Identity identity) {
        return use(identity);
    }

    public Optional<Identity> identity() {
        return identity;
    }

    public RequestBuilder onRequest() {
        return requestBuilder;
    }

    public ResponseBuilder onResponse() {
        return this.responseBuilder;
    }

    @Override
    public BlockingQueue<WebSocketMessage> queue() {
        return queue;
    }

    @Override
    public CompletableFuture<WebSocketMessage> subscribe(Consumer<WebSocketMessage> consumer) {
        return subscribe(SubscriptionFilter.ALL, consumer);
    }

    @Override
    public <T> T configureAs(Function<AutomationContext,T> f) {
        return f.apply(this);
    }

    @Override
    public HeartbeatContext heartbeatContext() {
        return heartbeatContext;
    }

    @Override
    public CompletableFuture<WebSocketMessage> subscribe(SubscriptionFilter filter) {
        return subscribe(filter, m -> {});
    }

    @Override
    public CompletableFuture<WebSocketMessage> subscribe(SubscriptionFilter filter, Consumer<WebSocketMessage> consumer) {
        CompletableFuture<WebSocketMessage> future = new CompletableFuture<>();
        Functions.despatch(() -> {
            try {
                WebSocketMessage message = queue.take();
                if(filter.accept(message)) {
                    consumer.accept(message);
                    future.complete(message);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public Filter asFilter() {
        return new RestAssuredFilter(requestBuilder.build(), responseBuilder.build());
    }

    public Optional<String> authToken() {
        return Optional.ofNullable(authToken);
    }

    public Optional<String> refreshToken() {
        return Optional.ofNullable(refreshToken);
    }

    public void authToken(String token) {
        authToken = token;
    }

    public void refreshToken(String token) {
        refreshToken = token;
    }

    public RequestSpecification when() {
        RequestSpecification req = RestAssured.given();
        requestLogger.accept(req.log());
        responseLogger.accept(req.response().log());
        return req.filter(asFilter());
    }

    @Override
    public Response get(Resource r) {
        return when().get(environment.orElse(Environment.DefaultEnvironment.getEnvironment()).toUri(r));
    }

    @Override
    public Response get(Resource r, Object... pathElements) {
        return when().get(environment.orElse(Environment.DefaultEnvironment.getEnvironment()).toUri(r.apply(pathElements)));
    }

    public <T> Response post(Resource r, T bodyContent) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return when().body(mapper.writeValueAsString(bodyContent)).post(environment.orElse(Environment.DefaultEnvironment.getEnvironment()).toUri(r));
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public URI toUri(Resource resource) {
        return env().toUri(resource);
    }

    public URI toUri(String protocol, Resource resource) {
        return env().toUri(protocol, resource);
    }

    private Environment env() {
        return this.environment.orElseGet(()-> getEnvironment());
    }

    public static abstract class NestedBuilder<T> {

        protected final Automat parent;

        public NestedBuilder(Automat parent) {
            this.parent = parent;
        }

        public final Automat apply(T t) {
            onApply(t);
            return this.parent;
        }

        protected abstract void onApply(T t);
    }

    public static class RequestBuilder extends NestedBuilder<UnaryOperator<FilterableRequestSpecification>> {

        private UnaryOperator<FilterableRequestSpecification> f = r -> r;

        public RequestBuilder(Automat parent) {
            super(parent);
        }

        @Override
        protected void onApply(UnaryOperator<FilterableRequestSpecification> f) {
            this.f = f;
        }

        public UnaryOperator<FilterableRequestSpecification> build() {
            return f;
        }
    }

    public static class ResponseBuilder extends NestedBuilder<MapBuilder<Integer, Function<RequestSpecification, Response>>> {

        private MapBuilder<Integer, Function<RequestSpecification, Response>> mapBuilder;

        public ResponseBuilder(Automat parent) {
            super(parent);
        }

        @Override
        protected void onApply(MapBuilder<Integer, Function<RequestSpecification, Response>> mapBuilder) {
            this.mapBuilder = mapBuilder;
        }

        public Map<Integer, Function<RequestSpecification, Response>> build() {
            Map<Integer, Function<RequestSpecification, Response>> newMap = new HashMap<>();
            Map<Integer, Function<RequestSpecification, Response>> map = Optional.ofNullable(mapBuilder).map(m -> m.build()).orElse(Collections.emptyMap());
            // transform
            map.entrySet().stream().forEach(e -> newMap.put(e.getKey(), e.getValue()));
            return newMap;
        }
    }

    public static class Utils {

        public static HttpCodeKey forHttpCode(int code) {
            return new HttpCodeKey(new HashMap(), code);
        };
    }

    public static class HttpCodeKey extends FilterFunctionValue {

        private final Integer code;

        protected HttpCodeKey(Map<Integer, Function<RequestSpecification, Response>> map, Integer code) {
            super(map);
            this.code = code;
        }

        public FilterFunctionValue use(Function<RequestSpecification, Response> f) {
            map.put(code, f);
            return new FilterFunctionValue(map, f);
        }

    }

    public static class FilterFunctionValue implements MapBuilder<Integer,Function<RequestSpecification,Response>> {

        protected final Map<Integer, Function<RequestSpecification, Response>> map;
        private Function<RequestSpecification, Response> f;

        protected FilterFunctionValue(Map<Integer, Function<RequestSpecification, Response>> map) {
            this.map = map;
        }

        protected FilterFunctionValue(Map<Integer, Function<RequestSpecification, Response>> map, Function<RequestSpecification, Response> f) {
            this(map);
            this.f = f;
        }

        public HttpCodeKey forHttpCode(int code) {
            return new HttpCodeKey(map, code);
        }

        @Override
        public Map<Integer, Function<RequestSpecification, Response>> build() {
            return map;
        }
    }

    private class RestAssuredFilter implements Filter {

        private final UnaryOperator<FilterableRequestSpecification> preHandler;
        private final Map<Integer, Function<RequestSpecification, Response>> postHandlers;

        public RestAssuredFilter(UnaryOperator<FilterableRequestSpecification> preHandler, Map<Integer, Function<RequestSpecification, Response>> postHandlers) {
            this.preHandler = preHandler;
            this.postHandlers = postHandlers;
        }

        private FilterableRequestSpecification preHandle(FilterableRequestSpecification requestSpec) {
            return this.preHandler.apply(requestSpec);
        }

        private Response postHandle(RequestSpecification requestSpec, Response response) {
            QueryableRequestSpecification q = SpecificationQuerier.query(requestSpec);
            Method.Replayer replayer = Method.valueOf(q.getMethod()).replayer(q.getURI());
            logger.info("response: "+response.statusCode());
            return Optional.ofNullable(postHandlers.get(response.statusCode()))
                    .map(f -> {
                        return f.andThen(r -> {
                            logger.info("replay original request: " + requestSpec);
                            return replayer.replay(requestSpec);
                        }).apply(RestAssured.given());
                    }).orElseGet(() ->response);
        }

        @Override
        public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            FilterableRequestSpecification req = preHandle(requestSpec);
            Response res = ctx.next(req, responseSpec);
            return postHandle(req, res);
        }
    }

}

