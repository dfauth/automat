package test;

import automat.MapBuilder;
import automat.Resource;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public enum TestResource implements Resource {

    AUTH("/api/user/login"), //, keys("username","password")),
    IDENTITY("/api/state/identity"),
    REGISTRATION("/api/client/registration"),
    SUBSCRIPTION("/api/ws-eventBus");

    protected final String uri;
    protected final Map<String, ?> map;

    TestResource(String uri) {
        this(uri, () -> Collections.emptyMap());
    }

    TestResource(String uri, MapBuilder<String, ?> builder) {
        this.uri = uri;
        this.map = builder.build();
    }

    public String uri() {
        return uri;
    }

    @Override
    public <A> String bodyContent(Function<A, Optional<String>> f) {
        return null;
    }

}
