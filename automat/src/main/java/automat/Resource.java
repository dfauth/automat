package automat;

public enum Resource {

    IDENTITY("/api/state/identity");

    protected final String uri;

    Resource(String uri) {
        this.uri = uri;
    }
}
