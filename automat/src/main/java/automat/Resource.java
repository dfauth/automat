package automat;

import java.util.function.Function;

public interface Resource {
    String uri();

    String bodyContent(Function<String, String> f);
}
