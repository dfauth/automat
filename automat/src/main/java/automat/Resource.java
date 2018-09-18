package automat;

import scala.PartialFunction;

import java.util.Optional;
import java.util.function.Function;

public interface Resource {

    String uri();

    <A> String bodyContent(Function<A, Optional<String>> f);

    default <A> String bodyContent(PartialFunction<A, String> f) {
        return bodyContent(lift(f));
    }

    default <A,B> Function<A,Optional<B>> lift(PartialFunction<A, B> f) {
        return a -> {
            if(f.isDefinedAt(a)) {
                return Optional.of(f.apply(a));
            } else {
                return Optional.empty();
            }
        };
    }

}
