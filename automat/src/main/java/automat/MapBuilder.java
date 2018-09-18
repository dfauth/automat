package automat;

import java.util.HashMap;
import java.util.Map;

@FunctionalInterface
public interface MapBuilder<K,V> {
    Map<K,V> build();

    static <K,V> Key<K,V> key(K k) {
        return new Key(k, new HashMap<K,V>());
    }

    public static class Key<K,V> {

        private final K k;
        private Map<K,V> map;

        public Key(K k, Map<K, V> map) {
            this.k = k;
            this.map = map;
        }

        public Value<K,V> value(V v) {
            this.map.put(k,v);
            return new Value(this.map);
        }
    }

    public static class Value<K,V> implements MapBuilder<K,V> {

        private Map<K, V> map;

        public Value(Map<K, V> map) {
            this.map = map;
        }

        public Key<K,V> key(K k) {
            return new Key(k, map);
        }

        @Override
        public Map<K, V> build() {
            return map;
        }
    }
}
