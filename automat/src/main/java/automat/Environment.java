package automat;

import java.net.URI;
import java.net.URISyntaxException;

public interface Environment {
    URI toUri(Resource resource);

    URI toUri(String protocol, Resource resource);

    public static class DefaultEnvironment implements Environment {

        private static Environment ENV = new DefaultEnvironment();

        private DefaultEnvironment() {}

        @Override
        public URI toUri(Resource resource) {
            return toUri("http", resource);
        }

        @Override
        public URI toUri(String protocol, Resource resource) {
            try {
                return new URI(protocol, null, "127.0.0.1", 8080, resource.uri(), resource.queryString(), null);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        public static Environment getEnvironment() {
            return ENV;
        }

        public static void setEnvironment(Environment environment) {
            ENV = environment;
        }
    }
}
