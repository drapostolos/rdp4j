package com.github.drapostolos.rdp4j;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
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

    private static final Logger LOG = LoggerFactory.getLogger(ScheduledRunnable.class);
    private final DirectoryPoller dp;
    private final Set<Poller> pollers = new CopyOnWriteArraySet<Poller>();
    private final ListenerNotifier notifier;
    private final ExecutorService executor;

    ScheduledRunnable(DirectoryPoller directoryPoller) {
        dp = directoryPoller;
        this.notifier = dp.notifier;
        for (PolledDirectory directory : dp.directories) {
            pollers.add(new Poller(dp, directory));
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
            notifier.beforePollingCycle(new BeforePollingCycleEvent(dp));
            if (!executor.isShutdown()) {
                executor.invokeAll(pollers);
            }
            notifier.afterPollingCycle(new AfterPollingCycleEvent(dp));
        } catch (InterruptedException e) {
            // allow thread to exit gracefully
        } catch (Throwable t) {
            LOG.error("Unexpected error!", t);
        }
    }

    void addListener(Rdp4jListener listener) {
        notifier.addListener(listener);
    }

    void removeListener(Rdp4jListener listener) {
        notifier.removeListener(listener);
    }

    void addDirectory(PolledDirectory directory) {
        pollers.add(new Poller(dp, directory));
    }

    void removeDirectory(PolledDirectory directory) {
        pollers.remove(new Poller(dp, directory));
    }

    void shutdown() {
        executor.shutdown();
    }

    void awaitTermination() {
        Util.awaitTermination(executor);
    }

    Set<PolledDirectory> getDirectories() {
        Set<PolledDirectory> result = new HashSet<PolledDirectory>();
        for (Poller poller : pollers) {
            result.add(poller.directory);
        }
        return result;
    }

}
