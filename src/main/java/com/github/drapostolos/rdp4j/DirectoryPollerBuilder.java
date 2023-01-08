package com.github.drapostolos.rdp4j;

import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.Persister;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * A builder class that configures and then returns a started
 * {@link DirectoryPoller} instance.
 * 
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public final class DirectoryPollerBuilder {
    private static final String NULL_ARGUMENT_ERROR_MESSAGE = "null argument not allowed!";
    static final String DEFAULT_THREAD_NAME = "DirectoryPoller-";
    Map<PolledDirectory, Set<CachedFileElement>> directories = new HashMap<>();

    // Optional settings, with default values:
    long pollingIntervalInMillis = 1000;
    FileFilter filter = new DefaultFileFilter();
    String threadName = DEFAULT_THREAD_NAME;
    boolean fileAddedEventEnabledForInitialContent = false;
    boolean parallelDirectoryPollingEnabled = false;
    Set<Rdp4jListener> listeners = new HashSet<Rdp4jListener>();

    DirectoryPollerBuilder() { // package-private access only.
    }

    /**
     * Enables a simple mechanism that persist the {@link PolledDirectory}'s state to 
     * the given <code>file</code>. As this library has no knowledge how to convert your
     * {@link PolledDirectory} implementation to and from a string, the client needs to 
     * supply the converter functions {@code dirToString} and <code>stringToDir</code>.
     * 
     * @see DirectoryPollerBuilder#enableStatePersisting(Persister)
     * 
     * @param persistedFile the files where to store persisted data.
     * @param dirToString a function that converts your {@link PolledDirectory} implementations to a string.
     * @param stringToDir a function that converts a string (as produced by <code>dirToString</code>) 
     * to an implementation of your {@link PolledDirectory}.
     * @return {@link DirectoryPollerBuilder}
     */
	public DirectoryPollerBuilder enableDefaultStatePersisting(Path persistedFile, 
			Function<PolledDirectory, String> dirToString, Function<String, PolledDirectory> stringToDir) {
        if (persistedFile == null) {
            throw new NullPointerException(NULL_ARGUMENT_ERROR_MESSAGE);
        }
        if (dirToString == null) {
            throw new NullPointerException(NULL_ARGUMENT_ERROR_MESSAGE);
        }
        if (stringToDir == null) {
            throw new NullPointerException(NULL_ARGUMENT_ERROR_MESSAGE);
        }
        if(Files.exists(persistedFile) && Files.isDirectory(persistedFile)) {
        	throw new IllegalStateException("Persisted file cannot be a directory: " + persistedFile.toAbsolutePath());
        }
		return enableStatePersisting(new SerializeToFilePersister(persistedFile, stringToDir, dirToString));
	}

	/**
	 * Provide your own {@link Persister} implementation. Any existing persisted data 
	 * will be read in {@link DirectoryPollerListener#beforeStart(BeforeStartEvent)}, i.e.
	 * before the {@link DirectoryPoller} starts.
	 * <p>
	 * The state of each {@link PolledDirectory} will be persisted in {@link DirectoryPollerListener#afterStop(AfterStopEvent)},
	 * i.e. after the {@link DirectoryPoller} has stopped.
	 * 
	 * @param persister Custom implementation of the {@link Persister} interface.
     * @return {@link DirectoryPollerBuilder}
	 */
	public DirectoryPollerBuilder enableStatePersisting(Persister persister) {
        if (persister == null) {
            throw new NullPointerException(NULL_ARGUMENT_ERROR_MESSAGE);
        }
		addListener(new StatePersister(persister));
		return this;
	}

    /**
     * Enable {@link FileAddedEvent} events to be fired for the initial
     * content of the directories added in the {@link DirectoryPoller}.
     * <p>
     * The initial content of a directory are the files/directories
     * it contains the first poll-cycle.
     * <p>
     * Optional setting. Disabled by default.
     * 
     * @return {@link DirectoryPollerBuilder}
     */
    public DirectoryPollerBuilder enableFileAddedEventsForInitialContent() {
        fileAddedEventEnabledForInitialContent = true;
        return this;
    }

    /**
     * Enable parallel polling of the directories added in the {@link DirectoryPoller}.
     * <p>
     * NOTE!
     * This puts constraints on the added listeners to be thread safe.
     * <p>
     * Optional setting. Disabled by default.
     * 
     * @return {@link DirectoryPollerBuilder}
     */
    public DirectoryPollerBuilder enableParallelPollingOfDirectories() {
        parallelDirectoryPollingEnabled = true;
        return this;
    }
    
	/**
     * Adds the given <code>directory</code> to the list of polled directories.
     * Mandatory to add at least one directory.
     * <p>
     * Once the {@link DirectoryPoller} has been built, it can be used to add
     * additional polled directories, or remove polled directories.
     * <p>
     * Optional to add {@link CachedFileElement}s that already exists in the
     * given <code>directory</code> and represent a state of a previous running
     * {@link DirectoryPoller}.
     * 
     * @param directory - the directory to poll.
	 * @param previousState - Any files/dirs that was known by a previous running {@link DirectoryPoller}
     * @throws NullPointerException - if the given argument is null.
     * @return {@link DirectoryPollerBuilder}
     * 
	 */
    public DirectoryPollerBuilder addPolledDirectory(PolledDirectory directory, CachedFileElement... previousState ) {
    	return addPolledDirectory(directory, new HashSet<>(asList(previousState)));
    }

    /**
     * Adds the given <code>directory</code> to the list of polled directories.
     * Mandatory to add at least one directory.
     * <p>
     * Once the {@link DirectoryPoller} has been built, it can be used to add
     * additional polled directories, or remove polled directories.
     * <p>
     * Optional to add {@link CachedFileElement}s that already exists in the
     * given <code>directory</code> and represent a state of a previous running
     * {@link DirectoryPoller}.
     * 
     * @param directory - the directory to poll.
	 * @param previousState - Any files/dirs that was known by a previous running {@link DirectoryPoller}
     * @throws NullPointerException - if the given argument is null.
     * @return {@link DirectoryPollerBuilder}
     * 
	 */
    public DirectoryPollerBuilder addPolledDirectory(PolledDirectory directory, Set<CachedFileElement> previousState) {
        if (directory == null) {
            throw new NullPointerException(NULL_ARGUMENT_ERROR_MESSAGE);
        }
        if (previousState == null) {
            throw new NullPointerException(NULL_ARGUMENT_ERROR_MESSAGE);
        }
        directories.putIfAbsent(directory, new LinkedHashSet<>());
        directories.get(directory).addAll(previousState);
        return this;
    }

    /**
     * Set the interval between each poll cycle. Optional parameter.
     * Default value is 1000 milliseconds.
     * 
     * @param interval - the interval between two poll-cycles.
     * @param timeUnit - the unit of the interval. Example: TimeUnit.MINUTES
     * @return {@link DirectoryPollerBuilder}
     * @throws IllegalArgumentException if <code>interval</code> is negative.
     */
    public DirectoryPollerBuilder setPollingInterval(long interval, TimeUnit timeUnit) {
        if (interval < 0) {
            throw new IllegalArgumentException("Argument 'interval' is negative: " + interval);
        }
        pollingIntervalInMillis = timeUnit.toMillis(interval);
        return this;
    }

    /**
     * Set a {@link FileFilter} to be used. Only {@link FileElement}'s
     * Satisfying the filter will be considered.
     * <p>
     * Optional setting. By default all {@link FileElement}'s are
     * satisfying the filter.
     * 
     * @param filter FileFilter
     * @return {@link DirectoryPollerBuilder}
     * @throws NullPointerException if <code>filter</code> is null.
     */
    public DirectoryPollerBuilder setDefaultFileFilter(FileFilter filter) {
        if (filter == null) {
            throw new NullPointerException(NULL_ARGUMENT_ERROR_MESSAGE);
        }
        this.filter = filter;
        return this;
    }

    /**
     * Changes the name of the associated polling thread to be equal
     * to the given <code>name</code>.
     * <p>
     * Optional setting. By default each thread is named "DirectoryPoller-{X}",
     * where {X} is a sequence number. I.e: "DirectoryPoller-1", "DirectoryPoller-2" etc. etc.
     * 
     * @param name of thread.
     * @return {@link DirectoryPollerBuilder}
     * @throws NullPointerException if the given argument is null.
     */
    public DirectoryPollerBuilder setThreadName(String name) {
        if (name == null) {
            String message = "Null argument";
            throw new NullPointerException(message);
        }
        threadName = name;
        return this;
    }

    /**
     * Adds the given <code>listener</code> to the list of {@link Rdp4jListener}'s
     * that receives notifications.
     * <p>
     * Once the {@link DirectoryPoller} has been built, it can be used to
     * add additional listeners, or remove listeners.
     * 
     * @param listener Implementation of any of the sub-interfaces of {@link Rdp4jListener}
     *        -interface.
     * @return {@link DirectoryPollerBuilder}
     * @throws NullPointerException if the given argument is null.
     */
    public DirectoryPollerBuilder addListener(Rdp4jListener listener) {
        if (listener == null) {
            throw new NullPointerException(NULL_ARGUMENT_ERROR_MESSAGE);
        }
        listeners.add(listener);
        return this;
    }

    /**
     * Builds a new {@link DirectoryPoller} instance and starts the poll-cycle
     * mechanism.
     * <p>
     * This method will block until all {@link BeforeStartEvent} events has been
     * fired (and processed by all listeners.)
     * 
     * @return {@link DirectoryPoller}.
     */
    public DirectoryPoller start() {
        return future().get();
    }

    /**
     * Asynchronously starts the poll-cycle mechanism.
     * <p>
     * To get a hold of the {@link DirectoryPoller} instance, call the get method on the returned
     * {@link DirectoryPollerFuture} instance.
     * 
     * @return {@link DirectoryPollerFuture}.
     */
    public DirectoryPollerFuture startAsync() {
        return future();
    }

    private DirectoryPollerFuture future() {
		Future<DirectoryPoller> f = Util.invokeTask("DP-BeforeStart", () -> {
			ListenerNotifier notifier = new ListenerNotifier(getLogger(ListenerNotifier.class), listeners);
			notifier.beforeStart(new BeforeStartEvent(this));
			/*
			 * The DirectoryPoller must be constructed after triggering
			 * "BeforeStartEvent", as registered listeners can add additional
			 * polled directories.
			 */
			return new DirectoryPoller(notifier, this).start();
		});
        return new DirectoryPollerFuture(f);
    }
}
