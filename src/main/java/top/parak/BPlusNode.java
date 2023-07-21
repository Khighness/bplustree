package top.parak;

import java.util.ArrayList;
import java.util.List;

/**
 * B+树节点
 *
 * @author Khighness
 * @since 2023-07-24
 */
@SuppressWarnings("all")
public class BPlusNode<K extends Comparable<K>, V> {

    private static final int NOT_FOUND = -1;

    /**
     * 是否根节点
     */
    private boolean isRoot;

    /**
     * 是否为叶子结点
     */
    private boolean isLeaf;

    /**
     * 父节点
     */
    private BPlusNode<K, V> parent;

    /**
     * 前驱节点
     */
    private BPlusNode<K, V> prev;

    /**
     * 后继节点
     */
    private BPlusNode<K, V> next;

    /**
     * KV列表
     */
    private List<Entry<K, V>> entries;

    /**
     * 子节点列表
     */
    private List<BPlusNode<K, V>> children;

    /**
     * 创建节点
     *
     * @param isLeaf 是否叶子节点
     */
    public BPlusNode(boolean isLeaf) {
        this.isLeaf = isLeaf;
        this.entries = new ArrayList<>();

        if (!isLeaf) {
            children = new ArrayList<>();
        }
    }

    /**
     * 创建节点
     *
     * @param isRoot 是否根节点
     * @param isLeaf 是否叶子节点
     */
    public BPlusNode(boolean isRoot, boolean isLeaf) {
        this(isLeaf);
        this.isRoot = isRoot;
    }

    /**
     * 查询
     *
     * @param key 键
     * @return 值
     */
    public V get(K key) {
        if (isLeaf) {
            int index = binarySearchInLeaf(key);
            if (index == NOT_FOUND) {
                return null;
            }
            return entries.get(index).getValue();
        }

        if (key.compareTo(firstKey()) < 0) {
            return firstChild().get(key);
        } else if (key.compareTo(lastKey()) >= 0) {
            return lastChild().get(key);
        } else {
            return children.get(binarySearchInInterior(key)).get(key);
        }
    }

    /**
     * 更新
     *
     * @param key   键
     * @param value 值
     * @param tree  树
     */
    public void put(K key, V value, BPlusTree<K, V> tree) {
        if (isLeaf) {
            int index = binarySearchInLeaf(key);

            if (index != NOT_FOUND || entries.size() < tree.getOrder()) {
                upsertLeaf(index, key, value);
                if (tree.getHeight() == 0) {
                    tree.setHeight(1);
                }
                return;
            }

            BPlusNode<K, V> left = new BPlusNode<>(true);
            BPlusNode<K, V> right = new BPlusNode<>(true);

            if (prev != null) {
                prev.next = left;
                left.prev = prev;
            }
            if (next != null) {
                next.prev = right;
                right.next = next;
            }
            if (prev == null) {
                tree.setHead(left);
            }

            left.next = right;
            right.prev = left;
            prev = null;
            next = null;

            splitLeaf(key, value, left, right, tree);

            if (parent != null) {
                int currIndex = parent.children.indexOf(this);
                parent.children.remove(this);

                left.parent = parent;
                right.parent = parent;

                parent.children.add(index, left);
                parent.children.add(index + 1, right);
                parent.entries.add(index, right.entries.get(0));
                entries = null;
                children = null;

                parent.splitIterior(tree);
                parent = null;
            } else {
                isRoot = false;
                BPlusNode<K, V> root = new BPlusNode<>(true, false);
                tree.setRoot(root);

                left.parent = root;
                right.parent = parent;

                parent.children.add(left);
                parent.children.add(right);
                parent.entries.add(right.entries.get(0));

                entries = null;
                children = null;
            }

            return;
        }

        if (key.compareTo(firstKey()) < 0) {
            firstChild().put(key, value, tree);
        } else if (key.compareTo(lastKey()) > 0) {
            lastChild().put(key, value, tree);
        } else {
            children.get(binarySearchInInterior(key)).put(key, value, tree);
        }
    }

    public V remove(K key, BPlusTree<K, V> tree) {
        if (isLeaf) {
            int index = binarySearchInLeaf(key);
            if (index == NOT_FOUND) {
                return null;
            }

            // 如果是根节点，直接删除
            if (isRoot) {
                if (entries.size() == 1) {
                    tree.setHeight(0);
                }
                return entries.remove(index).getValue();
            }

            // 如果关键字数 > M/2，直接删除
            if (entries.size() > tree.getOrder() / 2 && entries.size() > 2) {
                return entries.remove(index).getValue();
            }

            // 如果关键字数 < M/2，并且前驱节点关键字数 > M/2，则从前驱节点处借补
            if (prev != null
                    && prev.parent == parent
                    && prev.entries.size() > tree.getOrder() >> 1
                    && prev.entries.size() > 2) {
                entries.add(0, prev.entries.remove(prev.entries.size() - 1));
                int prevIndex = parent.children.indexOf(prev);
                parent.entries.set(index, entries.get(0));
                return removeLeaf(key);
            }

            // 如果关键字数 < M/2，并且后继节点关键字数 > M/2，则从后继节点处借补
            if (next != null
                    && next.parent == parent
                    && next.entries.size() > tree.getOrder() >> 1
                    && next.entries.size() > 2) {
                entries.add(next.entries.remove(0));
                int currIndex = parent.children.indexOf(this);
                parent.entries.set(currIndex, next.entries.get(0));
                return removeLeaf(key);
            }

            // 与前面节点合并
            if (prev != null
                    && prev.parent == parent
                    && (prev.entries.size() <= tree.getOrder() >> 1 || prev.entries.size() <= 2)) {
                V removedValue = removeLeaf(key);

                // 关键字合并
                for (int i = 0; i < entries.size(); i++) {
                    prev.entries.add(entries.get(i));
                }
                entries = prev.entries;
                parent.children.remove(prev);
                prev.parent = null;
                prev.entries = null;

                // 更新链表
                if (prev.prev != null) {
                    // 1.
                    BPlusNode<K, V> temp = prev;
                    temp.prev.next = this;
                    prev = temp.prev;
                    temp.prev = null;
                    temp.next = null;
                } else {
                    tree.setHead(this);
                    prev.next = null;
                    prev = null;
                }

                // 父节点中移除当前关键字
                parent.entries.remove(parent.children.indexOf(this));

                // 如果父节点仍然满足要求，则不需要调整:
                // 1. 父节点是根节点，父节点的子节点数量 >= 2。
                // 2. 父节点不是根节点，父节点的子节点数量 >= M/2 并且 >= 2。
                if (parent.isRoot && parent.children.size() >= 2
                        || !parent.isRoot && parent.children.size() >= tree.getOrder() >> 1 && parent.children.size() >= 2) {
                    return removedValue;
                }

                // 否则，调整父节点
                parent.adjust(tree);
                return removedValue;
            }

            // 与后面节点合并
            if (next != null
            && next.parent == parent
            && (next.entries.size() <= tree.getOrder() >> 1 || next.entries.size() <= 2)) {
                V removedValue = removeLeaf(key);
                for (int i = 0; i < next.entries.size(); i++) {
                    entries.add(next.entries.get(i));
                }
                next.parent = null;
                next.entries = null;
                parent.children.remove(next);
                if (next.next != null) {
                    BPlusNode<K, V> temp = next;
                    temp.next.prev = this;
                    next = temp.next;
                    temp.prev = null;
                    temp.next = null;
                } else {
                    next.prev = null;
                    next = null;
                }

                parent.entries.remove(parent.children.indexOf(this));
                if (parent.isRoot && parent.children.size() >= 2
                        || !parent.isRoot && (parent.children.size() >= tree.getOrder() >> 1)) {
                    return removedValue;
                }

                parent.adjust(tree);
                return removedValue;
            }
        }

        // TODO(Khighness)
        return null;
    }

    /**
     * 叶子节点关键字链表二分查找
     *
     * @param key 键
     * @return 存在则返回对应的下标，不存在则返回{@link #NOT_FOUND}
     */
    private int binarySearchInLeaf(K key) {
        int l = 0, h = entries.size() - 1, m = 0;
        int comp;
        while (l <= h) {
            m = (l + h) >> 1;
            comp = entries.get(m).getKey().compareTo(key);
            if (comp == 0) {
                return m;
            } else if (comp < 0) {
                l = m + 1;
            } else {
                h = m - 1;
            }
        }
        return NOT_FOUND;
    }

    /**
     * 内部节点关键字链表二分查找
     *
     * @param key 键
     * @return 比给定键大的第一个下标
     */
    private int binarySearchInInterior(K key) {
        int l = 0, h = entries.size() - 1, m = 0;
        int comp;
        while (l <= h) {
            m = (l + h) >> 1;
            comp = entries.get(m).getKey().compareTo(key);
            if (comp == 0) {
                return m + 1;
            } else if (comp < 0) {
                l = m + 1;
            } else {
                h = m - 1;
            }
        }
        return l;
    }

    /**
     * 插入/更新 键值
     *
     * @param index 下标
     * @param key   键
     * @param value 值
     */
    private void upsertLeaf(int index, K key, V value) {
        if (index != NOT_FOUND) {
            entries.get(index).setValue(value);
        } else {
            entries.add(new SimpleEntry<>(key, value));
        }
    }

    /**
     * 插入键值，叶子节点分裂
     *
     * @param key   键
     * @param value 值
     * @param left  分裂后的左叶子节点
     * @param right 分裂后的右叶子节点
     * @param tree  树
     */
    private void splitLeaf(K key, V value, BPlusNode<K, V> left, BPlusNode<K, V> right, BPlusTree<K, V> tree) {
        int leftSize = ((tree.getOrder() + 1) >> 1) + ((tree.getOrder() + 1) & 1);
        boolean inserted = false;

        for (int i = 0; i < entries.size(); i++) {
            if (leftSize != 0) {
                leftSize--;
                if (!inserted && entries.get(i).getKey().compareTo(key) > 0) {
                    left.entries.add(new SimpleEntry<>(key, value));
                    inserted = true;
                    i--;
                } else {
                    left.entries.add(entries.get(i));
                }
            } else {
                if (!inserted && entries.get(i).getKey().compareTo(key) > 0) {
                    right.entries.add(new SimpleEntry<>(key, value));
                    inserted = true;
                    i--;
                } else {
                    right.entries.add(entries.get(i));
                }
            }
        }

        if (!inserted) {
            right.entries.add(new SimpleEntry<>(key, value));
        }
    }

    /**
     * 如果内部节点的子节点数量超过M，需要分裂
     *
     * @param tree 树
     */
    private void splitIterior(BPlusTree<K, V> tree) {
        if (children.size() > tree.getOrder()) {
            BPlusNode<K, V> left = new BPlusNode<>(false);
            BPlusNode<K, V> right = new BPlusNode<>(false);

            int leftSize = ((tree.getOrder() + 1) >> 1) + ((tree.getOrder() + 1) & 1);
            int rightSize = (tree.getOrder() + 1) >> 1;

            for (int i = 0; i < leftSize; i++) {
                left.children.add(children.get(i));
                children.get(i).parent = left;
            }
            for (int i = 0; i < rightSize; i++) {
                right.children.add(children.get(leftSize + i));
                children.get(leftSize + i).parent = right;
            }
            for (int i = 0; i < leftSize - 1; i++) {
                left.entries.add(entries.get(i));
            }
            for (int i = 0; i < rightSize - 1; i++) {
                right.entries.add(entries.get(leftSize + i));
            }

            if (parent != null) {
                int currIndex = parent.children.indexOf(this);
                parent.children.remove(this);

                left.parent = parent;
                right.parent = parent;

                parent.children.add(currIndex, left);
                parent.children.add(currIndex + 1, right);
                parent.entries.add(currIndex, entries.get(leftSize - 1));
                entries = null;
                children = null;

                parent.splitIterior(tree);
                parent = null;
            } else {
                isRoot = false;
                BPlusNode<K, V> root = new BPlusNode<>(true, false);
                tree.setRoot(root);
                tree.setHeight(tree.getHeight() + 1);

                left.parent = root;
                right.parent = root;

                root.children.add(left);
                root.children.add(right);
                root.entries.add(entries.get(leftSize - 1));
                entries = null;
                children = null;
            }
        }
    }

    private void adjust(BPlusTree<K, V> tree) {
        // 如果子节点数 < M/2 或者 2，需要合并节点
        if (children.size() < tree.getOrder() >> 1 || children.size() < 2) {
            if (isRoot) {
                // 如果根节点满足要求
                if (children.size() >= 2) return;
                // 否则根节点只有一个子节点，将其设置为根节点
                BPlusNode<K, V> root = children.get(0);
                tree.setRoot(root);
                tree.setHeight(tree.getHeight() - 1);
                root.parent = null;
                root.isRoot = true;
                entries = null;
                children = null;
                return;
            }

            int currIndex = parent.children.indexOf(this);
            int prevIndex = currIndex - 1, nextIndex = currIndex + 1;
            BPlusNode<K, V> prev = null, next = null;
            if (prevIndex >= 0) {
                prev = parent.children.get(prevIndex);
            }
            if (nextIndex < parent.children.size()) {
                next = parent.children.get(nextIndex);
            }

            if (prev != null
                    && prev.children.size() > tree.getOrder() >> 1
                    && prev.children.size() > 2) {
                int prevLastIndex = prev.children.size() - 1;
                BPlusNode<K, V> borrow = prev.children.get(prevLastIndex);
                prev.children.remove(prevLastIndex);
                borrow.parent = this;
                children.add(0, borrow);
            }
        }

        // TODO(Khighness)
    }

    private V removeLeaf(K key) {
        int index = binarySearchInLeaf(key);
        if (index == NOT_FOUND) {
            return null;
        }
        return entries.remove(index).getValue();
    }

    private K firstKey() { return entries.get(0).getKey(); }
    private K lastKey() { return entries.get(entries.size() - 1).getKey(); }

    private BPlusNode<K, V> firstChild() { return children.get(0); }
    private BPlusNode<K, V> lastChild() { return children.get(children.size() - 1); }

}
