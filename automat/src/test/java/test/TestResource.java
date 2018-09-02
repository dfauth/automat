package test;

import automat.Resource;

public enum TestResource implements Resource {

    IDENTITY("/api/state/identity"),
    REGISTRATION("/api/client/registration");

    protected final String uri;

    TestResource(String uri) {
        this.uri = uri;
    }

    public String uri() {
        return uri;
    }
}
