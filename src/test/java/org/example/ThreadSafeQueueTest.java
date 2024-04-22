package org.example;

import org.junit.jupiter.api.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ThreadSafeQueueTest {

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private CustomQueue<Integer> queue;

    @BeforeEach
    void setUp() {
//        queue = new ThreadSafeQueue<>(10);
        queue = new ThreadSafeSemaphoreQueue<>(10);
    }

    @Test
    void emptyQueueIsEmpty() {
        assertEquals(0, queue.getCount(), "Queue not empty");
    }

    @Test
    void canPutThings() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            executor.execute(() -> {
                try {
                    queue.add(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        boolean threadsTerminated = executor.awaitTermination(5, TimeUnit.SECONDS);

        assertFalse(threadsTerminated);
        assertEquals(10, queue.getCount(), "Queue not empty");
    }
}