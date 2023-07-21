package top.parak;

/**
 * Simple KV entry implementation.
 *
 * @author Khighness
 * @since 2023-07-24
 */
public class SimpleEntry<K, V>  implements Entry<K, V> {

    private final K key;

    private V value;

    public SimpleEntry(K key, V value) {
        this.key   = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }

}
