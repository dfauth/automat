package test;

import automat.MapBuilder;
import automat.Resource;

import java.util.Collections;
import java.util.Map;

public enum TestResource implements Resource {

    AUTH("/api/user/login"), //, keys("username","password")),
    IDENTITY("/api/state/identity"),
    REGISTRATION("/api/client/registration"),
    SUBSCRIPTION("/api/stream");

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
}
