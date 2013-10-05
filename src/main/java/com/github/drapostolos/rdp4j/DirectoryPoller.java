package com.github.drapostolos.rdp4j;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * The DirectoryPoller, used for adding/removing {@link Rdp4jListener}s/{@link PolledDirectory}'s 
 * and terminating the poller mechanism. 
 * <p>
 * Simple usage example:
 * <pre>
 * 		DirectoryPoller dp = DirectoryPoller.newBuilder()
 * 		.addDirectory(new MyPolledDirectoryImp(...))
 * 		.addListener(new MyListenerImp())
 * 		.setPollingInterval(2, TimeUnit.SECONDS) // optional, 1 second default.
 * 		.setFileFilter(new MyFileFilterImp()) // optional, default matches all files.
 * 		.start();
 * 		
 * 		// do something
 * 
 * 		dp.stop();
 * 
 * </pre>
 * 
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public class DirectoryPoller {
	private static final long WITH_NO_DELAY = 0;
	private static AtomicInteger threadCount = new AtomicInteger();
	private PollerTask pollerTask;
	private FileFilter filter;
	private long pollingIntervalInMillis;
	private String threadName;

	// Below are passed to PollerTask
	Timer timer;
	ListenerNotifier notifier;
	boolean fileAddedEventEnabledForInitialContent;
	boolean parallelDirectoryPollingEnabled;
	Set<PolledDirectory> directories;

	/**
	 * Returns a new {@link DirectoryPollerBuilder}.
	 */
	public static DirectoryPollerBuilder newBuilder(){
		return new DirectoryPollerBuilder();
	}

	/* package-private access only */
	DirectoryPoller (DirectoryPollerBuilder builder){
		// First copy values from builder...
		directories = new LinkedHashSet<PolledDirectory>(builder.directories);
		filter = builder.filter;
		pollingIntervalInMillis = builder.pollingIntervalInMillis;
		threadName = builder.threadName;
		fileAddedEventEnabledForInitialContent = builder.fileAddedEventEnabledForInitialContent;
		parallelDirectoryPollingEnabled = builder.parallelDirectoryPollingEnabled;
		notifier = new ListenerNotifier(builder.listeners);

		// ...then check mandatory values
		if(directories.isEmpty()){
			String pollerName = DirectoryPoller.class.getSimpleName();
			String builderName = DirectoryPollerBuilder.class.getSimpleName();
			String message = 
					"Unable to build the '%s' when No directories has been added! "
					+ "You must add at least one directory before starting the '%s'.\n"
					+ "Call this method to add a directory: %s.addDirectory(PolledDirectory), "
					+ "before you can build the %s.";
			throw new IllegalStateException(String.format(message, pollerName, pollerName, builderName, pollerName));
		}

		setThreadName();
		pollerTask = new PollerTask(this);
	}
	private void setThreadName() {
		if(threadName.equals(DirectoryPollerBuilder.DEFAULT_THREAD_NAME)){
			threadName = threadName + threadCount.incrementAndGet();
		}
	}

	void start(){
		timer = new Timer(threadName);
		timer.schedule(pollerTask, WITH_NO_DELAY, pollingIntervalInMillis);
	}

	/**
	 * Stops the polling mechanism. After this method has been 
	 * called, there will be no more poll-cycles. If this method is called 
	 * during a poll-cycle it will block and wait for current poll-cycle to
	 * finish. 
	 * <p>
	 * Furthermore, it will block until all notifications to
	 * {@link DirectoryPollerListener#afterStop(AfterStopEvent)} has
	 * been processed.
	 */
	public void stop(){
		timer.cancel();
		pollerTask.waitForExecutionToStop();
		notifier.notifyListeners(new AfterStopEvent(this));
	}

	/**
	 * Returns the current {@link PolledDirectory}'s handled by 
	 * this instance.
	 */
	public Set<PolledDirectory> getPolledDirectories(){
		return pollerTask.getDirectories();
	}

	/**
	 * Returns the polling interval in milliseconds, as
	 * configured for this instance
	 */
	public long getPollingIntervalInMillis(){
		return pollingIntervalInMillis;
	}

	/**
	 * Returns the default {@link FileFilter}, as configured for this
	 * instance.
	 */
	public FileFilter getDefaultFileFilter(){
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
	 * 
	 * @param listener implementation of any of the sub-interfaces of {@link Rdp4jListener}.
	 */
	public void addListener(Rdp4jListener listener){
		if(listener == null){
			throw new NullPointerException("Argument is null.");
		}
		pollerTask.addListener(listener);
	}

	/**
	 * Removes the given <code>listener</code> from this instance.
	 * The <code>listener</code> will be removed after any ongoing 
	 * poll-cycles has finished. If there's no ongoing poll-cycles, 
	 * then the given <code>listener</code> will be removed just before 
	 * next poll-cycle is started.
	 * 
	 * @param listener implementation of any of the sub-interfaces of {@link Rdp4jListener}-interface.
	 * 
	 * @throws NullPointerException if the given argument is null.e
	 */
	public void removeListener(Rdp4jListener listener){
		if(listener == null){
			throw new NullPointerException("Argument is null.");
		}
		pollerTask.removeListener(listener);
	}

	/**
	 * Adds the given <code>directory</code> into this instance. 
	 * The <code>directory</code> will be polled in the next coming poll-cycle.
	 * <p>
	 * Registering an already registered directory will be ignored.
	 * 
	 * @param directory implementation of {@link PolledDirectory}.
	 * 
	 * @throws NullPointerException if the given argument is null.
	 */
	public void addPolledDirectory(PolledDirectory directory){
		if(directory == null){
			throw new NullPointerException("Argument is null.");
		}
		pollerTask.addDirectory(directory);
	}

	/**
	 * Removes the given <code>directory</code> from this instance. 
	 * The <code>directory</code> will be removed after any ongoing 
	 * poll-cycles has finished. If there's no ongoing poll-cycles, 
	 * then the given <code>directory</code> will be removed just before 
	 * next poll-cycle is started.
	 * 
	 * @param directory implementation of {@link PolledDirectory}.
	 * 
	 * @throws NullPointerException if the given argument is null.
	 */
	public void removePolledDirectory(PolledDirectory directory){
		if(directory == null){
			throw new NullPointerException("Argument is null.");
		}
		pollerTask.removeDirectory(directory);
	}

	/**
	 * Returns a string representation of this Directory monitor, 
	 * in the format: "{thread-name}: {directories} [polling every {polling-interval} milliseconds]" 
	 * 
	 */
	public String toString(){
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
	 * Returns the name of the associated polling thread.
	 */
	public String getThreadName() {
		return threadName;
	}

	/**
	 * Returns <code>true</code> if this {@link DirectoryPoller} has
	 * been configured to poll its directories in parallel, otherwise
	 * return false.
	 */
	public boolean isParallelDirectoryPollingEnabled() {
		return parallelDirectoryPollingEnabled;
	}

	/**
	 * Returns <code>true</code> if this {@link DirectoryPoller} has
	 * been configured to notify {@link DirectoryListener#fileAdded(FileAddedEvent)}
	 * method for the initial content of its directories, otherwise 
	 * returns false.
	 * <p>
	 * The initial content of a directory are the files/directories 
	 * it contains the first poll-cycle.
	 */
	public boolean isFileAdedEventForInitialContentEnabled() {
		return fileAddedEventEnabledForInitialContent;
	}
}