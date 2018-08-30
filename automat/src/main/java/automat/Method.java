package automat;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.function.BiFunction;

public enum Method {
    GET((s,u) -> s.get(u)),
    POST((s,u) -> s.post(u));

    private BiFunction<RequestSpecification, String, Response> biFunction;

    Method(BiFunction<RequestSpecification, String, Response> biFunction) {
        this.biFunction = biFunction;
    }

    public Replayer replayer(String uri) {
        return new Replayer(uri, this.biFunction);
    }

    public class Replayer {
        private final String uri;
        private final BiFunction<RequestSpecification, String, Response> f;

        public Replayer(String uri, BiFunction<RequestSpecification, String, Response> f) {
            this.uri = uri;
            this.f = f;
        }

        public Response replay(RequestSpecification spec) {
            return f.apply(spec, this.uri);
        }
    }
}

