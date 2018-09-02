package automat;

public enum Environment {
    LOCAL(9000);

    private static Environment instance;
    private final int port;
    private final String host;


    Environment() {
        this( 8080);
    }

    Environment(int port) {
        this("localhost", port);
    }

    Environment(String host, int port) {
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
}
