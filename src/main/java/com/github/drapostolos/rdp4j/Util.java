package com.github.drapostolos.rdp4j;

import static java.lang.Long.MAX_VALUE;
import static java.util.concurrent.TimeUnit.DAYS;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.drapostolos.rdp4j.spi.FileElement;

/**
 * Utility functions for internal usage.
 */
final class Util {

    private static Logger logger = LoggerFactory.getLogger(Util.class);

    Util() {
        throw new AssertionError("Not meant for instantiation");
    }

    static Set<FileElement> copyValuesToFileElementSet(Map<String, CachedFileElement> files) {
        HashSet<FileElement> result = new HashSet<FileElement>();
        for (CachedFileElement file : files.values()) {
            result.add(file.fileElement);
        }
        return result;
    }

    static LinkedHashMap<String, CachedFileElement> newLinkedHashMap() {
        return new LinkedHashMap<String, CachedFileElement>();
    }

    static void awaitTermination(ExecutorService executor) {
        while (true) {
            try {
                executor.awaitTermination(MAX_VALUE, DAYS);
                return;
            } catch (InterruptedException e) {
                // This will never happen.
                logger.warn("Thread interrupted, but ignored.");
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
