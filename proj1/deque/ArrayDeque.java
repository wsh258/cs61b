package deque;

import java.util.Iterator;

public class ArrayDeque <T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private int front;
    private int back;
    private int size;
    private static int INIT_CAPACITY = 8;


    public ArrayDeque() {
        items = (T[]) new Object[INIT_CAPACITY];
        front = 0;
        back = 1;
        size = 0;
    }
    private void resize(int newCapacity) {
        T[] newItems = (T[]) new Object[newCapacity];
        for(int i = 0;i<size();i++){
            newItems[i]  = get(i+1);
        }
        front = newCapacity-1;
        back = size();
        items  = newItems;
    }

    @Override
    public void addFirst(T item) {
        if (size()== items.length && items.length > INIT_CAPACITY){
            resize(size()*2);
        }
        items[front] = item;
        front =  Math.floorMod(front-1,items.length);
        size++;
    }

    @Override
    public void addLast(T item) {
        if (size()== items.length && items.length > INIT_CAPACITY){
            resize(size()*2);
        }
        items[back] = item;
        back =  Math.floorMod(back+1,items.length);
        size++;
    }

    @Override
    public int size() {
        return size;
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

    @Override
    public T removeFirst() {
        if(isEmpty())
            return null;
        if (size()<= items.length/4 && items.length > INIT_CAPACITY){
            resize(size()*2);
        }
        front =  Math.floorMod(front+1,items.length);
        T temp = items[front];
        items[front] = null;
        size--;
        return temp;

    }

    @Override
    public T removeLast() {
        if(isEmpty())
            return null;
        if (size()<= items.length/4 && items.length > INIT_CAPACITY){
            resize(size()*2);
        }
        back =  Math.floorMod(back-1,items.length);
        T temp = items[back];
        items[back] = null;
        size--;
        return temp;
    }


    @Override
    public T get(int index) {
        if(index>size||index<0)
            return null;
        return items[Math.floorMod(front+index,items.length)];
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>(){
            int pos=1;
            @Override
            public boolean hasNext() {

                return pos<size();
            }
            @Override
            public T next() {
                pos++;
                return get(pos);
            }
        };
    }
}
