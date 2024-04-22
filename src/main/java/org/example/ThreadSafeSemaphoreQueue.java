package org.example;

import java.util.concurrent.Semaphore;

public class ThreadSafeSemaphoreQueue<E> implements CustomQueue<E> {
    /* Semaphores to keep track of the number of available items and spaces
     * in the queue. */
    private final Semaphore numItems;
    private final Semaphore numSpaces;

    /* The maximum number of elements in the queue. */
    private final int capacity;

    /* The current number of elements in the queue. */
    private int count;

    public int getCount() {
        return count;
    }

    private final E[] items;

    /* The pointers for where to add/remove items to/from the queue. */
    private int addPointer;
    private int removePointer;

    public ThreadSafeSemaphoreQueue(int capacity) {
        this.capacity = capacity;
        items = (E[]) new Object[capacity];

        numItems = new Semaphore(0);
        numSpaces = new Semaphore(capacity);
    }

    public void add(E element) throws InterruptedException {
        numSpaces.acquire();
        addAux(element);
        numItems.release();
    }

    public E remove() throws InterruptedException {
        numItems.acquire();
        E item = removeAux();
        numSpaces.release();
        return item;
    }

    private synchronized void addAux(E element) {
        items[addPointer] = element;
        if (++addPointer == capacity) {
            addPointer = 0;
        }
        count++;
    }

    private synchronized E removeAux() {
        if (count == 0) {
            return null;
        }
        E element = items[removePointer];
        items[removePointer] = null;
        if (++removePointer == capacity) {
            removePointer = 0;
        }
        count--;
        return element;
    }
}
