package com.github.drapostolos.rdp4j;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.github.drapostolos.rdp4j.spi.FileElement;
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
	Set<PolledDirectory> directories = new LinkedHashSet<PolledDirectory>();
	
	// Optional settings, with default values:
	long pollingIntervalInMillis = 1000;
	FileFilter filter = new DefaultFileFilter();
	String threadName = DEFAULT_THREAD_NAME;
	boolean fileAddedEventEnabledForInitialContent = false;
	boolean parallelDirectoryPollingEnabled = false;
	Set<Rdp4jListener> listeners = new HashSet<Rdp4jListener>();
	

	DirectoryPollerBuilder(){ // package-private access only.
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
	public DirectoryPollerBuilder enableFileAddedEventsForInitialContent(){
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
	public DirectoryPollerBuilder enableParallelPollingOfDirectories(){
		parallelDirectoryPollingEnabled = true;
		return this;
	}
	
	/**
	 * Adds the given <code>directory</code> to the list of polled directories. 
	 * Mandatory to add at least one directory.
	 * <p>
	 * Once the {@link DirectoryPoller} has been built, it can be used to add 
	 * additional polled directories, or remove polled directories.
	 * 
	 * @param directory - the directory to poll.
	 * 
	 * @throws 	NullPointerException - if the given argument is null.
	 * 
	 * @return {@link DirectoryPollerBuilder}
	 */
	public DirectoryPollerBuilder addPolledDirectory(PolledDirectory directory) {
		if(directory == null){
			throw new NullPointerException(NULL_ARGUMENT_ERROR_MESSAGE);
		}
		directories.add(directory);
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
	 * 
	 * @return {@link DirectoryPollerBuilder}
	 * 
	 * @throws NullPointerException if <code>filter</code> is null.
	 */
	public DirectoryPollerBuilder setDefaultFileFilter(FileFilter filter) {
		if(filter == null){
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
	 * 
	 * @return {@link DirectoryPollerBuilder}
	 * 
	 * @throws NullPointerException if the given argument is null.
	 */
	public DirectoryPollerBuilder setThreadName(String name) {
		if(name == null){
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
	 * @param listener Implementation of any of the sub-interfaces of {@link Rdp4jListener}-interface.
	 * 
	 * @return {@link DirectoryPollerBuilder}
	 * 
	 * @throws NullPointerException if the given argument is null.
	 */
	public DirectoryPollerBuilder addListener(Rdp4jListener listener) {
		if(listener == null){
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
	 * 
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

    private DirectoryPollerFuture future(){
        final DirectoryPoller dp = new DirectoryPoller(this);

        Future<DirectoryPoller> f = Util.invokeTask("DP-BeforeStart", new Callable<DirectoryPoller>() {

            @Override
            public DirectoryPoller call() {
                dp.notifier.beforeStart(new BeforeStartEvent(dp));
                dp.start();
                return dp;
            }

        });
        return new DirectoryPollerFuture(f);
    }
}