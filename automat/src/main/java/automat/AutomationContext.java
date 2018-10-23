package automat;

import io.restassured.response.Response;
import io.restassured.specification.RequestLogSpecification;
import io.restassured.specification.ResponseLogSpecification;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;


public interface AutomationContext {

    void authToken(String authToken);

    Optional<String> authToken();

    void refreshToken(String refreshToken);

    Optional<Identity> identity();

    AutomationContext identity(Identity identity);

    URI toUri(Resource resource);

    URI toUri(String protocol, Resource resource);

    AutomationContext environment(Environment env);

    Response get(Resource r);

    Response get(Resource r, Object... pathElements);

    <T> Response post(Resource resource, T bodyContent);

    Automat.RequestBuilder onRequest();

    Automat.ResponseBuilder onResponse();

    BlockingQueue<WebSocketMessage> queue();

    CompletableFuture<WebSocketMessage> subscribe(SubscriptionFilter filter);

    CompletableFuture<WebSocketMessage> subscribe(SubscriptionFilter filter, Consumer<WebSocketMessage> consumer);

    CompletableFuture<WebSocketMessage> subscribe(Consumer<WebSocketMessage> consumer);

    <T> T configureAs(Function<AutomationContext,T> function);

    HeartbeatContext heartbeatContext();

    AutomationContext withHeartbeatInterval(Duration heartbeatInterval);

    Duration heartbeatInterval();

    AutomationContext async(WebSocketMessage message);

    WebSocketEndpoint webSocketEndpoint(Resource resource);

    default AutomationContext requestLogInstruction(Consumer<RequestLogSpecification> requestConsumer) {
        return logInstructions(requestConsumer, r->{});
    }

    default AutomationContext responseLogInstruction(Consumer<ResponseLogSpecification> responseConsumer) {
        return logInstructions(r->{}, responseConsumer);
    }

    AutomationContext logInstructions(Consumer<RequestLogSpecification> requestConsumer, Consumer<ResponseLogSpecification> responseConsumer);
}
