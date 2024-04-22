package org.example;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafeQueue<E> implements CustomQueue<E> {
    /* The maximum number of elements in the queue. */
    private final int capacity;

    /* The current number of elements in the queue. */
    private int count;

    private final E[] items;

    public ThreadSafeQueue(int capacity) {
        this.capacity = capacity;
        items = (E[]) new Object[capacity];
    }

    public int getCount() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    /* The pointers for where to add/remove items to/from the queue. */
    private int addPointer;
    private int removePointer;

    /* The underlying data structure to contain the queue items. */

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition notFull = lock.newCondition();

    private final Condition notEmpty = lock.newCondition();

    public void add(E item) throws InterruptedException {
        lock.lock();
        try {
            // Wait for the queue to not be full
            while (count == capacity) {
                notFull.await();
            }

            // Once ready, add the item to the queue
            items[addPointer] = item;

            // Handle the pointer maxing out by looping around since we are
            // using an array as the underlying data structure
            if (++addPointer == capacity) {
                addPointer = 0;
            }
            count++;

            // Let a dequeue thread take over the queue process
            notEmpty.signal();
        }
        finally {
            lock.unlock();
        }
    }

    public E remove() throws InterruptedException {
        lock.lock();
        try {
            // Wait for the queue to not be empty
            while (count == 0) {
                notEmpty.await();
            }

            // Once ready, remove the item from the queue
            E x = items[removePointer];
            items[removePointer] = null;

            // Handle the pointer maxing out by looping around since we are
            // using an array as the underlying data structure
            if (++removePointer == capacity) {
                removePointer = 0;
            }
            count--;

            // Let an enqueue thread take over the queue process
            notFull.signal();

            return x;
        } finally {
            lock.unlock();
        }
    }
}
