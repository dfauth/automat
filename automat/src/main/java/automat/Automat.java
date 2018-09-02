package automat;

import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Automat {

    private static final Logger logger = LogManager.getLogger(Automat.class);

    private RequestSpecification req = RestAssured.given();
    private final RequestBuilder requestBuilder = new RequestBuilder(this);
    private final ResponseBuilder responseBuilder = new ResponseBuilder(this);
    private String authToken;
    private String refreshToken;
    private Optional<Identity> identity = Optional.empty();

    public static Automat given() {
        return new Automat();
    }

    public Automat use(Environment environment) {
        Environment.setEnvironment(environment);
        return this;
    }

    public Automat environment(Environment environment) {
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
        return RestAssured.given().filter(asFilter()).port(Environment.getEnvironment().port());
    }

    public Response get(Resource r) {
        return when().get(r.uri());
    }

    public Response post(Resource r, String bodyContent) {
        return when().body(bodyContent).post(r.uri());
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

    public static class RequestBuilder extends NestedBuilder<Function<Automat, UnaryOperator<FilterableRequestSpecification>>> {

        private Function<Automat, UnaryOperator<FilterableRequestSpecification>> f = a -> r -> r;

        public RequestBuilder(Automat parent) {
            super(parent);
        }

        @Override
        protected void onApply(Function<Automat, UnaryOperator<FilterableRequestSpecification>> f) {
            this.f = f;
        }

        public UnaryOperator<FilterableRequestSpecification> build() {
            return f.apply(this.parent);
        }
    }

    public static class ResponseBuilder extends NestedBuilder<MapBuilder<Integer, Function<Automat, Function<FilterableRequestSpecification, Response>>>> {

        private MapBuilder<Integer, Function<Automat, Function<FilterableRequestSpecification, Response>>> mapBuilder;

        public ResponseBuilder(Automat parent) {
            super(parent);
        }

        @Override
        protected void onApply(MapBuilder<Integer, Function<Automat, Function<FilterableRequestSpecification, Response>>> mapBuilder) {
            this.mapBuilder = mapBuilder;
        }

        public Map<Integer, Function<FilterableRequestSpecification, Response>> build() {
            Map<Integer, Function<FilterableRequestSpecification, Response>> newMap = new HashMap<>();
            Map<Integer, Function<Automat, Function<FilterableRequestSpecification, Response>>> map = Optional.ofNullable(mapBuilder).map(m -> m.build()).orElse(Collections.emptyMap());
            // transform
            map.entrySet().stream().forEach(e -> newMap.put(e.getKey(), e.getValue().apply(parent)));
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

        protected HttpCodeKey(Map<Integer, Function<Automat, Function<FilterableRequestSpecification, Response>>> map, Integer code) {
            super(map);
            this.code = code;
        }

        public FilterFunctionValue use(Function<Automat, Function<FilterableRequestSpecification, Response>> f) {
            map.put(code, f);
            return new FilterFunctionValue(map, f);
        }

    }

    public static class FilterFunctionValue implements MapBuilder<Integer,Function<Automat,Function<FilterableRequestSpecification,Response>>> {

        protected final Map<Integer, Function<Automat, Function<FilterableRequestSpecification, Response>>> map;
        private Function<Automat, Function<FilterableRequestSpecification, Response>> f;

        protected FilterFunctionValue(Map<Integer, Function<Automat, Function<FilterableRequestSpecification, Response>>> map) {
            this.map = map;
        }

        protected FilterFunctionValue(Map<Integer, Function<Automat, Function<FilterableRequestSpecification, Response>>> map, Function<Automat, Function<FilterableRequestSpecification, Response>> f) {
            this(map);
            this.f = f;
        }

        public HttpCodeKey forHttpCode(int code) {
            return new HttpCodeKey(map, code);
        }

        @Override
        public Map<Integer, Function<Automat, Function<FilterableRequestSpecification, Response>>> build() {
            return map;
        }
    }

    interface MapBuilder<K,V> {
        Map<K,V> build();
    }

    private class RestAssuredFilter implements Filter {

        private final UnaryOperator<FilterableRequestSpecification> preHandler;
        private final Map<Integer, Function<FilterableRequestSpecification, Response>> postHandlers;

        public RestAssuredFilter(UnaryOperator<FilterableRequestSpecification> preHandler, Map<Integer, Function<FilterableRequestSpecification, Response>> postHandlers) {
            this.preHandler = preHandler;
            this.postHandlers = postHandlers;
        }

        private FilterableRequestSpecification preHandle(FilterableRequestSpecification requestSpec) {
            return this.preHandler.apply(requestSpec);
        }

        private Response postHandle(FilterableRequestSpecification requestSpec, Response response) {
            QueryableRequestSpecification q = SpecificationQuerier.query(requestSpec);
            Method.Replayer replayer = Method.valueOf(q.getMethod()).replayer(q.getURI());
            logger.info("response: "+response.statusCode());
            return Optional.ofNullable(postHandlers.get(response.statusCode()))
                    .map(f -> {
                        return f.andThen(r -> {
                            logger.info("replay original request: " + requestSpec);
                            return replayer.replay(requestSpec);
                        }).apply(requestSpec);
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

