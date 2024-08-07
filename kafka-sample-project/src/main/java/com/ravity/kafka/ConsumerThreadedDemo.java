package com.ravity.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class ConsumerThreadedDemo {

    static Logger logger = LoggerFactory.getLogger(ConsumerThreadedDemo.class);

    private ConsumerThreadedDemo() {
    }

    private void run() {
        logger.info("Starting Consumer Thread....");
        CountDownLatch latch = new CountDownLatch(1);
        Runnable runnable = new ConsumerRunnable(latch);
        Thread myThread = new Thread(runnable);
        myThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Caught shutdown hook...");
            ((ConsumerRunnable) runnable).shutdown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            logger.info("Application exited cleanly...");
        }));
    }

    public static void main(String[] args) {
        new ConsumerThreadedDemo().run();
    }
}

