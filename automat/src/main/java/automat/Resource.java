package automat;

import java.text.MessageFormat;

public interface Resource {

    String uri();

    default String queryString() {
        return null;
    }

    default Resource apply(Object... pathElements) {
        return () -> MessageFormat.format(Resource.this.uri(),pathElements);
    }
}
