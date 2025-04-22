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
        return new Iterator<T>() {
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

    private class Node{
        T item;
        Node prev;
        Node next;
        Node(T i,Node p,Node n){
            item = i;
            prev = p;
            next = n;
        }
    }
    private int size ;
    private final Node sentinel;
    private  Node CurrentNode;
    public LinkedListDeque() {
         sentinel = new Node(null, null, null);
         sentinel.next = sentinel;
         sentinel.prev = sentinel;
         CurrentNode =sentinel;

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
    public int size(){
        return size;
    }
    public boolean isEmpty(){
        return size==0;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder(10);
        for (T x : this) {
            stringBuilder.append(x);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }
    @Override
    public void printDeque(){


        System.out.printf("%s",this);

        System.out.println();
    }
    public T removeFirst(){
        if(sentinel.next==sentinel){
            return null;
        }
        T temp = sentinel.next.item;
        sentinel.next.next.prev=sentinel;
        sentinel.next=sentinel.next.next;
        size--;
        return temp;
    }
    public T removeLast(){
        if(sentinel.prev==sentinel){
            return null;
        }
        T temp = sentinel.prev.item;
        sentinel.prev.prev.next=sentinel;
        sentinel.prev=sentinel.prev.prev;
        size--;
        return temp;
    }

    public T get(int index){
        while (index !=0){
            index--;
            CurrentNode = CurrentNode.next;
            }
        T temp= CurrentNode.item;
        CurrentNode = sentinel;
        return temp;
    }
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Deque<?> other)) {
            return false;
        }

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

}
