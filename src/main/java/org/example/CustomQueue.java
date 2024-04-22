package org.example;

public interface CustomQueue<E> {
    public void add(E element) throws InterruptedException;
    public E remove() throws InterruptedException;
    public int getCount();
}
