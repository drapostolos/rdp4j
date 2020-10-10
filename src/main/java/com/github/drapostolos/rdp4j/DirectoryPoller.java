package com.github.drapostolos.rdp4j;

import static com.github.drapostolos.rdp4j.DirectoryPollerBuilder.DEFAULT_THREAD_NAME;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * The DirectoryPoller, used for adding/removing {@link Rdp4jListener}s/{@link PolledDirectory}'s
 * and terminating the poller mechanism.
 * <p>
 * Simple usage example:
 * <pre>
 *  DirectoryPoller dp = DirectoryPoller.newBuilder()
 *  .addDirectory(new MyPolledDirectoryImp(...))
 *  .addListener(new MyListenerImp())
 *  .setPollingInterval(2, TimeUnit.SECONDS) // optional, 1 second default.
 *  .setFileFilter(new MyFileFilterImp()) // optional, default matches all files.
 *  .start();
 *  
 *  // do something
 *  
 *  dp.stop();
 * 
 * </pre>
 * 
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public class DirectoryPoller {

    private static final String NULL_ARGUMENT_ERROR = "Argument is null.";
    private static final long WITH_NO_DELAY = 0;
    private static AtomicInteger threadCount = new AtomicInteger();
    private volatile boolean shouldInvokeShutdownTask = true;
    private ScheduledRunnable scheduledRunnable;
    private FileFilter filter;
    private long pollingIntervalInMillis;
    private String threadName;
    private CountDownLatch latch = new CountDownLatch(1);
    private ScheduledExecutorService executor;

    // Below are passed to PollerTask
    ListenerNotifier notifier;
    boolean fileAddedEventEnabledForInitialContent;
    boolean parallelDirectoryPollingEnabled;
    Set<PolledDirectory> directories;

    /* package-private access only */
    DirectoryPoller(DirectoryPollerBuilder builder) {
        // First copy values from builder...
        directories = new LinkedHashSet<>(builder.directories);
        filter = builder.filter;
        pollingIntervalInMillis = builder.pollingIntervalInMillis;
        threadName = builder.threadName;
        fileAddedEventEnabledForInitialContent = builder.fileAddedEventEnabledForInitialContent;
        parallelDirectoryPollingEnabled = builder.parallelDirectoryPollingEnabled;
        notifier = new ListenerNotifier(builder.listeners);

        // ...then check mandatory values
        if (directories.isEmpty()) {
            String pollerName = DirectoryPoller.class.getSimpleName();
            String builderName = DirectoryPollerBuilder.class.getSimpleName();
            String message = "Unable to start the '%s' when No directories has been added! "
                    + "You must add at least one directory before starting the '%s'.\n"
                    + "Call this method to add a directory: %s.addPolledDirectory(PolledDirectory), "
                    + "before you can start the %s.";
            throw new IllegalStateException(String.format(message, pollerName, pollerName, builderName, pollerName));
        }

        if (threadName.equals(DEFAULT_THREAD_NAME)) {
            threadName = threadName + threadCount.incrementAndGet();
        }
        scheduledRunnable = new ScheduledRunnable(this);
    }

    /**
     * @return a new {@link DirectoryPollerBuilder}.
     */
    public static DirectoryPollerBuilder newBuilder() {
        return new DirectoryPollerBuilder();
    }

    void start() {
        executor = Executors.newScheduledThreadPool(1, new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName(threadName);
                return t;
            }
        });
        executor.scheduleAtFixedRate(scheduledRunnable, WITH_NO_DELAY, pollingIntervalInMillis, MILLISECONDS);
    }

    /**
     * Stops the polling mechanism. There will be no more new poll-cycles after this method has
     * returned. If this method is called during a poll-cycle it will block and wait for current
     * poll-cycle to finish gracefully (This also includes waiting for any listener methods
     * to finish gracefully).
     * <p>
     * Furthermore, it will block until all notifications to
     * {@link DirectoryPollerListener#afterStop(AfterStopEvent)} has been processed.
     * <p>
     * If a previous call has been made to {@link #stopAsync()}, {@link #stopAsyncNow()} and
     * the last poll-cycle is not finished, then this method will block until the above
     * conditions are met.
     * <p>
     * Subsequent calls to this method have no affect.
     */
    public void stop() {
        stopAsync();
        awaitTermination();
    }

    /**
     * Stops the polling mechanism. There will be no more new poll-cycles after this method has
     * returned. If this method is called during a poll-cycle it will block and wait for current
     * poll-cycle to finish. Underlying threads will be interrupted (this also includes any
     * listener methods).
     * <p>
     * Furthermore, it will block until all notifications to
     * {@link DirectoryPollerListener#afterStop(AfterStopEvent)} has been processed.
     * <p>
     * If a previous call has been made to {@link #stopAsync()}, {@link #stopAsyncNow()} and
     * the last poll-cycle is not finished, then this method will block until the above
     * conditions are met.
     * <p>
     * Subsequent calls to this method have no affect.
     */
    public void stopNow() {
        stopAsyncNow();
        awaitTermination();
    }

    /**
     * Stops the polling mechanism, but does not wait for any ongoing poll-cycles to finish.
     * Any ongoing poll-cycles will finish gracefully. Use the following method to wait for
     * termination: {@link #awaitTermination()}.
     * <p>
     * Subsequent calls to this method have no affect.
     */
    public void stopAsync() {
        executor.shutdown();
        invokeShutdownTaskOnce();
    }

    /**
     * Stops the polling mechanism, but does not wait for any ongoing poll-cycles to finish.
     * Underlying threads will be interrupted (this also includes any
     * listener methods). Use the following method to wait for termination:
     * {@link #awaitTermination()}.
     * <p>
     * Subsequent calls to this method have no affect.
     */
    public void stopAsyncNow() {
        executor.shutdownNow();
        invokeShutdownTaskOnce();
    }

    private synchronized void invokeShutdownTaskOnce() {
        if (shouldInvokeShutdownTask) {
            final DirectoryPoller dp = this;

            Util.invokeTask("DP-AfterStop", new Callable<Void>() {

                @Override
                public Void call() {
                    Util.awaitTermination(executor);
                    scheduledRunnable.shutdown();
                    scheduledRunnable.awaitTermination();
                    notifier.afterStop(new AfterStopEvent(dp));
                    latch.countDown();
                    return null;
                }
            });
        }
        shouldInvokeShutdownTask = false;
    }

    /**
     * Blocks until the last poll-cycle has finished and all {@link AfterStopEvent} has been
     * processed.
     */
    public void awaitTermination() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            String message = "awaitTermination() method was interrupted!";
            throw new UnsupportedOperationException(message, e);
        }
    }

    /**
     * @return {@code true} if this {@link DirectoryPoller} is terminated, otherwise {@code false}.
     */
    public boolean isTerminated() {
        return latch.getCount() == 0;
    }

    /**
     * @return the current {@link PolledDirectory}'s handled by
     *         this instance.
     */
    public Set<PolledDirectory> getPolledDirectories() {
        return scheduledRunnable.getDirectories();
    }

    /**
     * @return the polling interval in milliseconds, as
     *         configured for this instance
     */
    public long getPollingIntervalInMillis() {
        return pollingIntervalInMillis;
    }

    /**
     * @return the default {@link FileFilter}, as configured for this
     *         instance.
     */
    public FileFilter getDefaultFileFilter() {
        return filter;
    }

    /**
     * Adds the given <code>listener</code> into this instance.
     * The <code>listener</code> will start receiving notifications
     * in the next coming poll-cycle.
     * <p>
     * Any {@link DirectoryPollerListener} added with this method
     * will not have it's {@link DirectoryPollerListener#beforeStart(BeforeStartEvent)}
     * method triggered, as at this point the {@link DirectoryPoller}
     * is already started.
     * <p>
     * Registering an already registered listener will be ignored.
     * 
     * @throws NullPointerException if given <code>listener</code> is null.
     * @param listener implementation of any of the sub-interfaces of {@link Rdp4jListener}.
     */
    public void addListener(Rdp4jListener listener) {
        if (listener == null) {
            throw new NullPointerException(NULL_ARGUMENT_ERROR);
        }
        scheduledRunnable.addListener(listener);
    }

    /**
     * Removes the given <code>listener</code> from this instance.
     * The <code>listener</code> will be removed after any ongoing
     * poll-cycles has finished. If there's no ongoing poll-cycles,
     * then the given <code>listener</code> will be removed just before
     * next poll-cycle is started.
     * 
     * @param listener implementation of any of the sub-interfaces of {@link Rdp4jListener}
     *        -interface.
     * @throws NullPointerException if the given argument is null.e
     */
    public void removeListener(Rdp4jListener listener) {
        if (listener == null) {
            throw new NullPointerException(NULL_ARGUMENT_ERROR);
        }
        scheduledRunnable.removeListener(listener);
    }

    /**
     * Adds the given <code>directory</code> into this instance.
     * The <code>directory</code> will be polled in the next coming poll-cycle.
     * <p>
     * Registering an already registered directory will be ignored.
     * 
     * @param directory implementation of {@link PolledDirectory}.
     * @throws NullPointerException if the given argument is null.
     */
    public void addPolledDirectory(PolledDirectory directory) {
        if (directory == null) {
            throw new NullPointerException(NULL_ARGUMENT_ERROR);
        }
        scheduledRunnable.addDirectory(directory);
    }

    /**
     * Removes the given <code>directory</code> from this instance.
     * The <code>directory</code> will be removed after any ongoing
     * poll-cycles has finished. If there's no ongoing poll-cycles,
     * then the given <code>directory</code> will be removed just before
     * next poll-cycle is started.
     * 
     * @param directory implementation of {@link PolledDirectory}.
     * @throws NullPointerException if the given argument is null.
     */
    public void removePolledDirectory(PolledDirectory directory) {
        if (directory == null) {
            throw new NullPointerException(NULL_ARGUMENT_ERROR);
        }
        scheduledRunnable.removeDirectory(directory);
    }

    /**
     * Returns a string representation of this Directory monitor,
     * in the format: "{thread-name}: {directories} [polling every {polling-interval} milliseconds]"
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(getThreadName())
                .append(": ")
                .append(getPolledDirectories())
                .append(" [polling every: ")
                .append(getPollingIntervalInMillis())
                .append(" milliseconds]");
        return sb.toString();
    }

    /**
     * @return the name of the associated polling thread.
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * @return <code>true</code> if this {@link DirectoryPoller} has
     *         been configured to poll its directories in parallel, otherwise
     *         return false.
     */
    public boolean isParallelDirectoryPollingEnabled() {
        return parallelDirectoryPollingEnabled;
    }

    /**
     * @return <code>true</code> if this {@link DirectoryPoller} has
     *         been configured to notify {@link DirectoryListener#fileAdded(FileAddedEvent)}
     *         method for the initial content of its directories, otherwise
     *         returns false.
     *         <p>
     *         The initial content of a directory are the files/directories
     *         it contains the first poll-cycle.
     */
    public boolean isFileAdedEventForInitialContentEnabled() {
        return fileAddedEventEnabledForInitialContent;
    }
}
