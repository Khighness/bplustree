package top.parak;

/**
 * 键值
 *
 * @author Khighness
 * @since 2023-07-24
 */
public interface Entry<K, V> {

    /**
     * 获取键
     *
     * @return 键
     */
    K getKey();

    /**
     * 获取值
     *
     * @return 值
     */
    V getValue();

    /**
     * 重设值
     *
     * @param value 新值
     * @return 旧值
     */
    V setValue(V value);

}
