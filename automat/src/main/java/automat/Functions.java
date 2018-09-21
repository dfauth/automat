package automat;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.CloseReason;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static automat.Automat.given;

public class Functions {

    private static final Logger logger = LogManager.getLogger(Functions.class);

    public static UnaryOperator<FilterableRequestSpecification> authHandler = r -> {
        AutomationContext ctx = given();
        logger.info("authHandler");
        ctx.authToken().<Void>map(t -> {
            r.header("Authorization", "Bearer "+t);
            return null;
        });
        return r;
    };

    public static Function<Response, Response> subscribeTo(Resource resource, Consumer<WebSocketEndpoint> consumer, Consumer<String> messageConsumer, Consumer<CloseReason> closeConsumer) {
        AutomationContext ctx = given();
        return r-> {
            if(r.statusCode() == 200) {
                logger.info("subscribing to "+resource);
                // subscribe
                WebSocketEndpoint endPoint = new WebSocketEndpoint(given(), ctx.toUri("ws", resource));
                endPoint.onOpen(s->consumer.accept(endPoint));
                endPoint.onText(t->messageConsumer.accept(t));
                endPoint.onClose(q->closeConsumer.accept(q));
                endPoint.start();
            }
            return r;
        };
    }

    public static Function<Response, Response> storeToken  = r -> {
        logger.info("storeToken");
        AutomationContext ctx = given();
        ctx.authToken(r.body().jsonPath().getString("authToken"));
        ctx.refreshToken(r.body().jsonPath().getString("refreshToken"));
        return r;
    };

    public static Function<FilterableRequestSpecification, Response> loginHandler(Resource resource) {
        AutomationContext ctx = given();
        return r -> {
            RequestSpecification tmp = r.contentType(ContentType.JSON).body(IdentityBean.of(ctx.identity()));
            Response res = tmp.log().all().post(ctx.toUri(resource));
            logger.info("loginHandler response statusCode: "+res.statusCode());
            r.then().statusCode(200);
            return res;
        };
    }

}
