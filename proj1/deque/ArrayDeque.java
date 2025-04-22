package deque;

import java.util.Iterator;
import java.util.Objects;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private int front;
    private int back;
    private int size;
    private static final int INIT_CAPACITY = 8;

    public ArrayDeque() {
        items = (T[]) new Object[INIT_CAPACITY];
        front = 0;
        back = 1;
        size = 0;
    }
    private void resize(int newCapacity) {
        T[] newItems = (T[]) new Object[newCapacity];
        for (int i = 0; i < size(); i++){
            newItems[i] = get(i);
        }
        front = newCapacity - 1;
        back = size();
        items  = newItems;
    }

    @Override
    public void addFirst(T item) {
        if (size() == items.length) {
            resize(size() * 2);
        }
        items[front] = item;
        front = Math.floorMod(front - 1, items.length);
        size++;
    }

    @Override
    public void addLast(T item) {
        if (size() == items.length ){
            resize(size() * 2);
        }
        items[back] = item;
        back = Math.floorMod(back + 1, items.length);
        size++;
    }

    @Override
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
        System.out.printf("%s", stringBuilder);
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (isEmpty())
            return null;
        if (size()<= items.length/4 && items.length >= INIT_CAPACITY){
            resize(size() * 2);
        }
        front =  Math.floorMod(front + 1, items.length);
        T temp = items[front];
        items[front] = null;
        size--;
        return temp;
    }

    @Override
    public T removeLast() {
        if(isEmpty())
            return null;
        if (size()<= items.length / 4 && items.length >= INIT_CAPACITY) {
            resize(size() * 2);
        }
        back = Math.floorMod(back - 1, items.length);
        T temp = items[back];
        items[back] = null;
        size--;
        return temp;
    }


    @Override
    public T get(int index) {
        if(index>=size||index<0) {
            return null;
        }
        return items[Math.floorMod(front + index + 1, items.length)];
    }

    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int count = 0;
            @Override
            public boolean hasNext() {
                return count < size;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    return null;}
                T item = get(count);
                count++;
                return item;
            }
        };
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
}
