package automat;

import automat.events.CloseEvent;
import automat.events.MessageEvent;
import automat.events.OpenEvent;

public abstract class WebSocketEventHandler<T> {
    public void handle(OpenEvent<T> event) {}

    public void handle(MessageEvent<T> event) {}

    public void handle(CloseEvent<T> event) {}

}
