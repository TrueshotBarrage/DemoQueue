package org.example;

import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;


public class MultithreadedTest {

    private CustomQueue<Integer> queue;

    @Test
    void removeBlocksWhenEmpty() {
        queue = new ThreadSafeQueue<>(10);
        AtomicBoolean shouldNotHaveBeenReached = new AtomicBoolean(false);
        Thread removeThread = new Thread(() -> {
            try {
                int noItem = queue.remove();
                shouldNotHaveBeenReached.set(true);
            } catch (InterruptedException success) {

            }
        });
        try {
            removeThread.start();
            Thread.sleep(5000);
            removeThread.interrupt();
            removeThread.join(5000);
            assertFalse(removeThread.isAlive());
        } catch (Exception unexpected) {
            shouldNotHaveBeenReached.set(true);
        }

        assertFalse(shouldNotHaveBeenReached.get());
    }

    @Test
    void addBlocksWhenFull() {
        queue = new ThreadSafeQueue<>(10);
        boolean addToQueueFailure = false;
        for (int i = 0; i < 10; i++) {
            try {
                queue.add(i);
            } catch (InterruptedException failure) {
                addToQueueFailure = true;
            }
        }
        assertFalse(addToQueueFailure);

        AtomicBoolean shouldNotHaveBeenReached = new AtomicBoolean(false);
        Thread addThread = new Thread(() -> {
            try {
                queue.add(42);
                shouldNotHaveBeenReached.set(true);
            } catch (InterruptedException success) {

            }
        });
        try {
            addThread.start();
            Thread.sleep(5000);
            addThread.interrupt();
            addThread.join(5000);
            assertFalse(addThread.isAlive());
        } catch (Exception unexpected) {
            shouldNotHaveBeenReached.set(true);
        }

        assertFalse(shouldNotHaveBeenReached.get());
    }

    @Test
    void queueCapacityOneFourBlockedThreads() {
        queue = new ThreadSafeSemaphoreQueue<>(1);
        boolean shouldNotHaveBeenReached = false;
        Thread[] threads = new Thread[5];
        boolean[] threadsBlocked = new boolean[5];
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                try {
                    queue.add(finalI);
                } catch (InterruptedException blocked) {
                    threadsBlocked[finalI] = true;
                }
            });
            try {
                threads[i].start();
            } catch (Exception unexpected) {
                shouldNotHaveBeenReached = true;
            }
        }

        try {
            Thread.sleep(5000);
        } catch (Exception unexpected) {
            shouldNotHaveBeenReached = true;
        }

        for (int i = 0; i < 5; i++) {
            try {
                threads[i].interrupt();
                threads[i].join(5000);
            } catch (Exception unexpected) {
                shouldNotHaveBeenReached = true;
            }
        }

        assertFalse(shouldNotHaveBeenReached);

        int trueCount = 0;
        for (int i = 0; i < 5; i++) {
            if (threadsBlocked[i]) {
                trueCount++;
            }
        }
        assertEquals(4, trueCount);
    }
}
