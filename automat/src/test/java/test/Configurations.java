package test;

import automat.Automat;
import automat.AutomationContext;

import java.util.function.Function;

import static automat.Automat.Utils.forHttpCode;
import static automat.Functions.*;
import static test.TestResource.AUTH;
import static test.TestResource.SUBSCRIPTION;

public class Configurations {

    public static Function<AutomationContext, Automat> basicClientWithWebSocket = c -> c.onRequest().
            apply(authHandler).
            onResponse().
            apply(
                forHttpCode(403).
                    use(
                        loginHandler(AUTH)
                        .andThen(storeToken)
                        .andThen(subscribeTo(
                            SUBSCRIPTION,
                            heartbeatConsumer(c.queue())
                        ) // subscribeTo
                    ) // andThen
                ) // use
            );// apply;
}
