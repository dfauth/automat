package automat;

import java.net.URI;
import java.net.URISyntaxException;

public enum Environment {
    LOCAL(9000);

    private static Environment instance;
    private final int port;
    private final String host;
    private final String protocol;


    Environment() {
        this( 8080);
    }

    Environment(int port) {
        this("http", "localhost", port);
    }

    Environment(String protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public static Environment getEnvironment() {
        if(instance == null) {
            instance = LOCAL;
        }
        return instance;
    }

    public static void setEnvironment(Environment environment) {
        instance = environment;
    }

    public int port() {
        return port;
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
