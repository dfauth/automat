package automat;

import io.restassured.response.Response;

import java.net.URI;
import java.util.Optional;


public interface AutomationContext {

    void authToken(String authToken);

    Optional<String> authToken();

    void refreshToken(String refreshToken);

    Optional<Identity> identity();

    AutomationContext identity(Identity identity);

    URI toUri(Resource resource);

    URI toUri(String protocol, Resource resource);

    AutomationContext environment(Environment env);

    <T> Response post(Resource resource, T bodyContent);

    Automat.RequestBuilder onRequest();

    Automat.ResponseBuilder onResponse();
}
