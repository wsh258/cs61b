package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {


    public class BST<K extends Comparable<K>, V> {
        private Node root;             // root of BST
        private int size = 0;
        private class Node {
            private K key;           // sorted by key
            private V val;         // associated data
            private Node left, right;  // left and right subtrees

            public Node(K key, V val) {
                this.key = key;
                this.val = val;
            }
        }

        private void add(K key,V val) {
            root = addRecursive(root, key, val);
        }
        private Node addRecursive(Node currentNode, K key, V val) {
            if (currentNode == null) {
                size++;
                return new Node(key, val);
            }
            int cmp = key.compareTo(currentNode.key);
            if (cmp > 0) {
                currentNode.right = addRecursive(currentNode.right, key, val);
            } else if (cmp < 0) {
                currentNode.left = addRecursive(currentNode.left, key, val);
            } else {
                currentNode.val = val;
            }
            return currentNode;
        }
        private Node find(K k) {
            return findRecursive(root, k);
        }

        private Node findRecursive(Node current, K key) {
            if (current == null) {
                return null;
            }
            int cmp = key.compareTo(current.key);
            if (cmp < 0) {
                current = current.left;
            } else if (cmp > 0) {
                current = current.right;
            } else {
                return current;
            }
            return findRecursive(current, key);
        }
        private void delete(K key) {
             root = deleteRecursive(root, key);
        }
        private Node deleteRecursive(Node current,K key) {
            if (current == null) {
                root = null;
            }
            int cmp;
            if (current != null) {
                cmp = key.compareTo(current.key);
                //需要删除的节点小于当前节点
                if (cmp < 0) {
                    current.left = deleteRecursive(current.left, key);
                } else if (cmp > 0) {
                    current.right = deleteRecursive(current.right, key);
                } else {//找到要删除的节点
                    size--;
                    if (current.left == null && current.right == null) {
                        return null;
                    } else if (current.left == null) {
                        return current.right;
                    } else if (current.right == null) {
                        return current.left;
                    } else {//有两个子节点
                        Node temp = findMax(current.left);
                        current.key = temp.key;
                        current.val = temp.val;
                        current.left = deleteRecursive(current.left, temp.key);
                    }
                }
            }
            return current;
        }
        private Node findMax(Node node) {
            while (node != null) {
                node = node.right;
            }
            return node;
        }
        private int size() {
            return  size;
        }
    }
    BST<K, V> bst;
    BSTMap() {
        bst = new BST<>();
    }


    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public void clear() {
        bst = new BST<>();
    }

    @Override
    public boolean containsKey(K key) {
        return bst.find(key) != null;
    }

    @Override
    public V get(K key) {
        if(containsKey(key)) {
            return bst.find(key).val;
        }
        return null;
    }

    @Override
    public int size() {
        return bst.size();
    }

    @Override
    public void put(K key, V value) {
        if (!containsKey(key)) {
            bst.add(key, value);
        }
    }

    @Override
    public Set<K> keySet() {
        return Set.of();
    }


    @Override
    public V remove(K key) {
        BST<K, V>.Node temp = bst.find(key);
        if (temp == null){
            return null;
        }
        bst.delete(key);
        return temp.val;
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<K> iterator() {
        return null;
    }
}
