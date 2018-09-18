package test;

import automat.Resource;

import java.util.function.Function;

public enum TestResource implements Resource {

    AUTH("/api/user/login"),
    IDENTITY("/api/state/identity"),
    REGISTRATION("/api/client/registration"),
    SUBSCRIPTION("/api/ws-eventBus");

    protected final String uri;

    TestResource(String uri) {
        this.uri = uri;
    }

    public String uri() {
        return uri;
    }

    @Override
    public String bodyContent(Function<String, String> f) {
        return null;
    }

}
