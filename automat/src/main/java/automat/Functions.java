package automat;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Functions {

    private static final Logger logger = LogManager.getLogger(Functions.class);

    public static UnaryOperator<FilterableRequestSpecification> authHandler = r -> {
        Automat ctx = Automat.given();
        ctx.authToken().<Void>map(t -> {
            r.header("Authorization", "Bearer "+t);
            return null;
        });
        return r;
    };

    public static Function<Response, Response> subscribeTo(Resource resource) {
        Automat ctx = Automat.given();
        return r-> {
            if(r.statusCode() == 200) {
                logger.info("subscribing to "+resource);
                // subscribe
                WebSocketEndpoint client = websocketClient(ctx.toUri("ws", resource));
            }
            return r;
        };
    }

    public static Function<Response, Response> storeToken  = r -> {
        Automat ctx = Automat.given();
        ctx.authToken(r.body().jsonPath().getString("authToken"));
        ctx.refreshToken(r.body().jsonPath().getString("refreshToken"));
        return r;
    };

    public static Function<FilterableRequestSpecification, Response> loginHandler(Resource resource) {
        Automat ctx = Automat.given();
        return r -> {
//            String jsonString = "{\n\t\"username\": \"" + ctx.identity().map(i -> i.username()).orElse(null) + "\",\n\t\"password\": \"" + ctx.identity().map(i -> i.password()).orElse(null) + "\"\n}";
//            String jsonString = resource.bodyContent(IdentityHelper$.MODULE$.extractIdentityFields(ctx.identity().get()));
//            logger.info("fire loginHandler: "+jsonString);
            RequestSpecification tmp = r.contentType(ContentType.JSON).body(IdentityBean.of(ctx.identity()));
            Response res = tmp.log().all().post(ctx.toUri(resource));
            logger.info("res: "+res.statusCode());
            r.then().statusCode(200);
            return res;
        };
    }

    private static WebSocketEndpoint websocketClient(URI uri) {
        try {
            WebSocketEndpoint endPoint = new WebSocketEndpoint((t,s)->{
                logger.info("WebSocketEndpoint: Message received from server:"+t);
            });
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(endPoint, uri);
            return endPoint;
        } catch (DeploymentException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
