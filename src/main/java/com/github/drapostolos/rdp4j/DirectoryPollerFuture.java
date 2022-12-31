package com.github.drapostolos.rdp4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * This class represents a {@link DirectoryPoller} instance that is about to start.
 * Use this class to:
 * <ul>
 * <li>Ask if the {@link DirectoryPoller} has started or not.</li>
 * <li>Retrieve the {@link DirectoryPoller} instance after it has been started.
 * </ul>
 */
public final class DirectoryPollerFuture {

    private final Future<DirectoryPoller> future;

    DirectoryPollerFuture(Future<DirectoryPoller> future) {
        this.future = future;
    }

    /**
     * Retrieves the {@link DirectoryPoller} instance.
     * This method will block until all {@link BeforeStartEvent} events has been
     * fired (and processed by all listeners.)
     * 
     * @return a started {@link DirectoryPoller} instance.
     */
	public DirectoryPoller get() {
		try {
			return future.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			}
			throw new IllegalStateException(e);
		}
	}

    /**
     * @return true if {@link DirectoryPoller} has started, otherwise false.
     */
    public boolean isStarted() {
        return future.isDone();
    }

}
