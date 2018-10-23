package test;

import automat.Environment;
import automat.Resource;

import java.net.URI;
import java.net.URISyntaxException;

public enum TestEnvironment implements Environment {
    LOCAL(9000);

    private static TestEnvironment instance;
    private final int port;
    private final String host;
    private final String protocol;


    TestEnvironment() {
        this( 8080);
    }

    TestEnvironment(int port) {
        this("http", "localhost", port);
    }

    TestEnvironment(String protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public static TestEnvironment getEnvironment() {
        if(instance == null) {
            instance = LOCAL;
        }
        return instance;
    }

    public static void setEnvironment(TestEnvironment environment) {
        instance = environment;
    }

    public URI toUri(Resource resource) {
        return toUri(protocol, resource);
    }

    public URI toUri(String protocol, Resource resource) {
        try {
            return new URI(protocol, null, host, port, resource.uri(), null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
