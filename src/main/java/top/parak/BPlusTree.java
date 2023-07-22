package top.parak;

/**
 * B+树实现
 * <b><p>M阶B+树的性质：</p></b>
 * <ul>
 *     <li>任意非叶子节点最多有M个子节点</li>
 *     <li>除根节点以外的非叶子节点至少有 (M+1)/2 个子节点</li>
 *     <li>根节点至少有2个子节点</li>
 *     <li>除根节点外每个节点存放 [(M-1)/2, M-1] 个关键字</li>
 *     <li>非叶子节点的子节点数量比关键字多1个</li>
 *     <li>非叶子节点的所有key按升序存放</li>
 *     <li>所有叶子节点位于同一层</li>
 * </ul>
 *
 * @author Khighness
 * @since 2023-07-24
 */
public class BPlusTree<K extends Comparable<K>, V> {

    private static final int MIN_ORDER = 3;

    /**
     * 阶数
     */
    protected int order;

    /**
     * 根节点
     */
    protected BPlusNode<K, V> root;

    /**
     * 高度
     */
    private int height;

    /**
     * 叶子节点的链表头
     */
    private BPlusNode<K, V> head;

    public BPlusTree(int order) {
        if (order < MIN_ORDER) {
            throw new IllegalArgumentException("order must be greater than " + MIN_ORDER);
        }

        this.order = order;
        this.root = new BPlusNode<>(true, true);
        this.head = this.root;
    }

    public int getOrder() {
        return order;
    }

    public BPlusNode<K, V> getRoot() {
        return root;
    }

    public void setRoot(BPlusNode<K, V> root) {
        this.root = root;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public BPlusNode<K, V> getHead() {
        return head;
    }

    public void setHead(BPlusNode<K, V> head) {
        this.head = head;
    }

    public V get(K key) {
        return root.get(key);
    }

    public void set(K key, V value) {
        root.put(key, value, this);
    }

    public V remove(K key) {
        return root.remove(key, this);
    }

}
