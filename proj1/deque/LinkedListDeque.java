package deque;
import java.util.Iterator;
import java.util.Objects;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Node currentNode = sentinel;

            @Override
            public boolean hasNext() {
                return currentNode.next != sentinel;
            }

            @Override
            public T next() {
                currentNode = currentNode.next;
                return currentNode.item;
            }
        };
    }

    private class Node {
        T item;
        Node prev;
        Node next;
        Node(T i, Node p, Node n) {
            item = i;
            prev = p;
            next = n;
        }
    }
    private int size ;
    private final Node sentinel;
    public LinkedListDeque() {
        sentinel = new Node(null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;

    }
    public void addFirst(T item){
        sentinel.next = new Node(item, sentinel, sentinel.next);
        sentinel.next.next.prev = sentinel.next;
        size++;
    }
    public void addLast(T item){
        sentinel.prev = new Node(item, sentinel.prev, sentinel);
        sentinel.prev.prev.next = sentinel.prev;
        size++;
    }
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        StringBuilder stringBuilder = new StringBuilder(10);
        for (T x : this) {
            stringBuilder.append(x);
            stringBuilder.append(" ");
        }
        System.out.printf("%s",stringBuilder);
        System.out.println();
    }
    public T removeFirst() {
        if (sentinel.next == sentinel) {
            return null;
        }
        T temp = sentinel.next.item;
        sentinel.next.next.prev=sentinel;
        sentinel.next=sentinel.next.next;
        size--;
        return temp;
    }
    public T removeLast() {
        if (sentinel.prev == sentinel) {
            return null;
        }
        T temp = sentinel.prev.item;
        sentinel.prev.prev.next = sentinel;
        sentinel.prev = sentinel.prev.prev;
        size--;
        return temp;
    }

    public T get(int index) {
        if (size() == 0) {
            return null;
        }
        Node CurrentNode = this.sentinel.next;
        while (index != 0){
            index--;
            CurrentNode = CurrentNode.next;
        }
        return CurrentNode.item;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Deque<?>)) {
            return false;
        }
        Deque<?> other = (Deque<?>) o;
        if (other.size() != this.size()) {
            return false;
        }

        for (int i = 0; i < size(); i++) {
            Object thisItem = this.get(i);
            Object otherItem = other.get(i);

            if (!Objects.equals(thisItem, otherItem)) {
                return false;
            }
        }
        return true;
    }

    public T getRecursive(int index) {
        if (index < 0 || index > size) {
            return null;
        }
        return getRecursiveHelper(sentinel.next, index);
    }

    private T getRecursiveHelper(Node node, int index) {
        if (index == 0) {
            return node.item;
        }
        return getRecursiveHelper(node.next, index - 1);
    }
}
