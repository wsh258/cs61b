package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author Sihao Wong
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private int bucketSize = 16;
    private double loadFactor = 0.75;
    private int size = 0;
    /** Constructors */
    public MyHashMap() {
        buckets = createTable(bucketSize);
    }

    public MyHashMap(int initialSize) {
        bucketSize = initialSize;
        buckets = createTable(bucketSize);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        bucketSize = initialSize;
        loadFactor = maxLoad;
        buckets = createTable(initialSize);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            table[i] = createBucket();  // 使用统一工厂方法创建桶
        }
        return table;
    }
    private int getIndex(K key) {
        return Math.floorMod(key.hashCode(), bucketSize);
    }
    // Your code won't compile until you do so!
    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public void clear() {
        size = 0;
        buckets = createTable(bucketSize);
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param key
     */
    @Override
    public boolean containsKey(K key) {
        int index = getIndex(key);
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     *
     * @param key
     */
    @Override
    public V get(K key) {
        int index = getIndex(key);
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }
    /**
     * Returns the number of key-value mappings in this map.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key,
     * the old value is replaced.
     *
     * @param key
     * @param value
     */
    @Override
    public void put(K key, V value) {
        int index = getIndex(key);

        if (containsKey(key)){
            for (Node node : buckets[index]) {
                if (node.key.equals(key)) {
                    node.value = value;
                }
            }
        }
        else {
            size++;
            buckets[index].add(createNode(key, value));
            if ((double) buckets[index].size() / bucketSize >= loadFactor) {
                bucketSize *= 2;
                resize(bucketSize);

            }
        }
    }
    private void resize(int newSize) {
        Collection<Node>[] oldBuckets = buckets;
        buckets = createTable(newSize);
        size = 0; // 将 size 重置，在重新插入时递增
        for (Collection<Node> bucket : oldBuckets) {
            for (Node node : bucket) {
                put(node.key, node.value); // 重新插入元素，会更新 size
            }
        }
    }

    /**
     * Returns a Set view of the keys contained in this map.
     */
    @Override
    public Set<K> keySet() {
        HashSet<K> set = new HashSet<>();
        for(K k :this){
            set.add(k);
        }
        return set;
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     * Not required for Lab 8. If you don't implement this, throw an
     * UnsupportedOperationException.
     *
     * @param key
     */
    @Override
    public V remove(K key) {
        int index = getIndex(key);
        Node node;
            Iterator<Node> it = buckets[index].iterator();
            while (it.hasNext()) {
                node = it.next();
                if (node.key.equals(key)) {
                    V temp = node.value;
                    it.remove();
                    size--;
                    return temp;
                }
            }
        return null;
    }


    /**
     * Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 8. If you don't implement this,
     * throw an UnsupportedOperationException.
     *
     * @param key
     * @param value
     */
    @Override
    public V remove(K key, V value) {
        int index = getIndex(key);
        Iterator<Node> it = buckets[index].iterator();
        Node node;
        while (it.hasNext()) {
            node = it.next();
            if (node.key.equals(key) && node.value.equals(value)) {
                V temp = node.value;
                it.remove();
                size--;
                return temp;
                }
            }
        return null;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<K> iterator() {
        return new Iterator<K>() {
            int bucketIndex = 0;
            Iterator<Node> bucketIterator = advanceToNextNonEmptyBucket();

            // 前进到下一个非空 bucket，并返回其迭代器
            private Iterator<Node> advanceToNextNonEmptyBucket() {
                while (bucketIndex < buckets.length) {
                    if (!buckets[bucketIndex].isEmpty()) {
                        return buckets[bucketIndex].iterator();
                    }
                    bucketIndex++;
                }
                return null;
            }
            @Override
            public boolean hasNext() {
                if (bucketIterator == null) return false;
                if (bucketIterator.hasNext()) {
                    return true;
                } else {
                    bucketIndex++;
                    bucketIterator = advanceToNextNonEmptyBucket();
                    return bucketIterator != null && bucketIterator.hasNext();
                }
            }

            @Override
            public K next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return bucketIterator.next().key;
            }
        };
    }

}
