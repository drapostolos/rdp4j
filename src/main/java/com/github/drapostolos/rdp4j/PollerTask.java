package com.github.drapostolos.rdp4j;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.drapostolos.adp4j.spi.PolledDirectory;

/**
 * This is the timer thread which is executed every n milliseconds
 * according to the setting of the directory poller. It investigates the
 * directory in question and notify listeners if files are added/removed/modified,
 * or if IO Error has been raised/ceased.
 */


final class PollerTask extends TimerTask {
	private static Logger logger = LoggerFactory.getLogger(PollerTask.class);
	private final DirectoryPoller dp;
	private final Map<PolledDirectory, Poller> pollers = new LinkedHashMap<PolledDirectory, Poller>();
	private final ListenerNotifier notifier;
	private Queue<Adp4jListener> listenersToRemove = new ConcurrentLinkedQueue<Adp4jListener>();
	private Queue<Adp4jListener> listenersToAdd = new ConcurrentLinkedQueue<Adp4jListener>();
	private Queue<PolledDirectory> directoriesToAdd = new ConcurrentLinkedQueue<PolledDirectory>();
	private Queue<PolledDirectory> directoriesToRemove = new ConcurrentLinkedQueue<PolledDirectory>();;
	final ExecutorService executor;

	PollerTask(DirectoryPoller directoryPoller) {
		dp = directoryPoller;
		this.notifier = dp.notifier;
		for(PolledDirectory directory : dp.directories){
			pollers.put(directory, new Poller(directoryPoller, directory));
		}
		if(dp.parallelDirectoryPollingEnabled){
			executor = Executors.newCachedThreadPool();
		} else {
			executor = Executors.newSingleThreadExecutor();
		}
	}

	/**
	 * This method is periodically called by the {@link java.util.Timer} instance.
	 */
	public synchronized void run(){
		addRemoveListeners();
		addRemoveDirectories();
		notifier.notifyListeners(new BeforePollingCycleEvent(dp));
		try {
			executor.invokeAll(pollers.values());
		} catch (InterruptedException e) {
			logger.error(
					"Internal poller thread of the {} was interrupted. " +
					"Interruption is ignored! To stop the {} call its stop() " +
					"method: {}.stop().",
					DirectoryPoller.class.getSimpleName(),
					DirectoryPoller.class.getSimpleName(),
					DirectoryPoller.class.getSimpleName()
					);
		}
		notifier.notifyListeners(new AfterPollingCycleEvent(dp));
		addRemoveListeners();
		addRemoveDirectories();
	}

	private void addRemoveDirectories() {
		PolledDirectory directory;
		while((directory = directoriesToAdd.poll()) != null){
			if(!pollers.containsKey(directory)){
				pollers.put(directory, new Poller(dp, directory));
			}
		}
		while((directory = directoriesToRemove.poll()) != null){
			pollers.remove(directory);
		}
	}

	private void addRemoveListeners() {
		Adp4jListener listener;
		while((listener = listenersToAdd.poll()) != null){
			notifier.addListener(listener);
		}
		while((listener = listenersToRemove.poll()) != null){
			notifier.removeListener(listener);
		}
	}

	void addListener(Adp4jListener listener) {
		listenersToAdd.add(listener);
	}

	void removeListener(Adp4jListener listener) {
		listenersToRemove.add(listener);
	}

	void addDirectory(PolledDirectory directory) {
		directoriesToAdd.add(directory);
	}

	void removeDirectory(PolledDirectory listener) {
		directoriesToRemove.add(listener);
	}


	synchronized void waitForExecutionToStop() {
		/*
		 * This method is called after the Timer has been canceled.
		 * If there is an ongoing poll while timer is canceled, this
		 * method will block until the last poll has finished executing
		 * (since both this and the run() methods are synchronized).
		 */
	}

	synchronized Set<PolledDirectory> getDirectories() {
		return new LinkedHashSet<PolledDirectory>(pollers.keySet());
	}

}