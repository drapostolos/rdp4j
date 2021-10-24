package com.github.drapostolos.rdp4j;

import static java.lang.Long.MAX_VALUE;
import static java.util.concurrent.TimeUnit.DAYS;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility functions for internal usage.
 */
final class Util {

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    Util() {
        throw new AssertionError("Not meant for instantiation");
    }

    static void awaitTermination(ExecutorService executor) {
        while (true) {
            try {
                executor.awaitTermination(MAX_VALUE, DAYS);
                return;
            } catch (InterruptedException e) {
            	Thread.currentThread().interrupt();
                LOG.warn("Thread interrupted, but ignored.");
            }
        }
    }

    static <T> Future<T> invokeTask(String threadName, Callable<T> callable) {
        FutureTask<T> task = new FutureTask<T>(callable);
        Thread t = new Thread(task);
        t.setName(threadName);
        t.start();
        return task;
    }

}
