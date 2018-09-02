package automat;

import java.util.Map;


public abstract class Value<K,V> {


    protected final Map<K, V> map;

    public Value(Map<K, V> map) {
        this.map = map;
    }

    protected Key<K,V> key(K k) {
        return createKey(map, k);
    }

    protected abstract <U extends Key<K,V>> U createKey(Map<K, V> map, K k);

    public static abstract class Key<K,V> extends Value <K,V> {

        private final K k;

        public Key(Map<K,V> map, K k) {
            super(map);
            this.k = k;
        }

        protected Value<K,V> value(V v) {
            map.put(k,v);
            return createValue(map);
        }

        protected abstract <T extends Value<K,V>> T createValue(Map<K, V> map);
    }
}
