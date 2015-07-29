package com.github.drapostolos.rdp4j;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * This is the timer thread which is executed every n milliseconds
 * according to the setting of the directory poller. It investigates the
 * directory in question and notify listeners if files are added/removed/modified,
 * or if IO Error has been raised/ceased.
 */
final class ScheduledRunnable implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ScheduledRunnable.class);
    private final DirectoryPoller dp;
    private final Map<PolledDirectory, Poller> pollers = new LinkedHashMap<PolledDirectory, Poller>();
    private final ListenerNotifier notifier;
    private Queue<Rdp4jListener> listenersToRemove = new ConcurrentLinkedQueue<Rdp4jListener>();
    private Queue<Rdp4jListener> listenersToAdd = new ConcurrentLinkedQueue<Rdp4jListener>();
    private Queue<PolledDirectory> directoriesToAdd = new ConcurrentLinkedQueue<PolledDirectory>();
    private Queue<PolledDirectory> directoriesToRemove = new ConcurrentLinkedQueue<PolledDirectory>();;
    private final ExecutorService executor;

    ScheduledRunnable(DirectoryPoller directoryPoller) {
        dp = directoryPoller;
        this.notifier = dp.notifier;
        for (PolledDirectory directory : dp.directories) {
            pollers.put(directory, new Poller(dp, directory));
        }
        if (dp.parallelDirectoryPollingEnabled) {
            executor = Executors.newCachedThreadPool();
        } else {
            executor = Executors.newSingleThreadExecutor();
        }
    }

    /**
     * This method is periodically called by the {@link ExecutorService}.
     */
    @Override
    public synchronized void run() {
        try {
            addRemoveListeners();
            addRemoveDirectories();

            notifier.notifyListeners(new BeforePollingCycleEvent(dp));
            if (!executor.isShutdown()) {
                executor.invokeAll(pollers.values());
            }
            notifier.notifyListeners(new AfterPollingCycleEvent(dp));
            addRemoveListeners();
            addRemoveDirectories();
        } catch (InterruptedException e) {
            // allow thread to exit
        } catch (Throwable t) {
            logger.error("Unexpected error!", t);
        }
    }

    private void addRemoveDirectories() {
        PolledDirectory directory;
        while ((directory = directoriesToAdd.poll()) != null) {
            if (!pollers.containsKey(directory)) {
                pollers.put(directory, new Poller(dp, directory));
            }
        }
        while ((directory = directoriesToRemove.poll()) != null) {
            pollers.remove(directory);
        }
    }

    private void addRemoveListeners() {
        Rdp4jListener listener;
        while ((listener = listenersToAdd.poll()) != null) {
            notifier.addListener(listener);
        }
        while ((listener = listenersToRemove.poll()) != null) {
            notifier.removeListener(listener);
        }
    }

    void addListener(Rdp4jListener listener) {
        listenersToAdd.add(listener);
    }

    void removeListener(Rdp4jListener listener) {
        listenersToRemove.add(listener);
    }

    void addDirectory(PolledDirectory directory) {
        directoriesToAdd.add(directory);
    }

    void removeDirectory(PolledDirectory listener) {
        directoriesToRemove.add(listener);
    }

    void shutdown() {
        executor.shutdown();
    }

    void awaitTermination() {
        Util.awaitTermination(executor);
    }

    synchronized Set<PolledDirectory> getDirectories() {
        return new LinkedHashSet<PolledDirectory>(pollers.keySet());
    }

}
