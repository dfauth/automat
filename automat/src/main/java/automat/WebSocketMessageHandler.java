package automat;

import automat.messages.ApplicationMessage;
import automat.messages.HeartbeatMessage;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class WebSocketMessageHandler<T> {

    public Optional<T> handle(HeartbeatMessage message) {return Optional.empty();}

    public Optional<T> handle(ApplicationMessage message) {return Optional.empty();}

    public static <T> WebSocketMessageHandler<T> heartbeatMessageHandler(Function<HeartbeatMessage,T> function) {
        return new WebSocketMessageHandler<T>(){
            @Override
            public Optional<T> handle(HeartbeatMessage message) {
                return Optional.of(function.apply(message));
            }
        };
    }

    public static <T> WebSocketMessageHandler<T> applicationMessageHandler(Function<ApplicationMessage,T> function) {
        return new WebSocketMessageHandler<T>(){
            @Override
            public Optional<T> handle(ApplicationMessage message) {
                return Optional.of(function.apply(message));
            }
        };
    }

    public static WebSocketMessageHandler heartbeatMessageHandler(Consumer<HeartbeatMessage> consumer) {
        return new WebSocketMessageHandler(){
            @Override
            public Optional<Void> handle(HeartbeatMessage message) {
                consumer.accept(message);
                return Optional.empty();
            }
        };
    }

    public static WebSocketMessageHandler applicationMessageHandler(Consumer<ApplicationMessage> consumer) {
        return new WebSocketMessageHandler(){
            @Override
            public Optional<Void> handle(ApplicationMessage message) {
                consumer.accept(message);
                return Optional.empty();
            }
        };
    }
}
