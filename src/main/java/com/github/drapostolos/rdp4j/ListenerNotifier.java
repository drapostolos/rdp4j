package com.github.drapostolos.rdp4j;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;

class ListenerNotifier {
	private final Logger logger;
    final Set<Rdp4jListener> listeners = new CopyOnWriteArraySet<Rdp4jListener>();

    ListenerNotifier(Logger logger, Set<Rdp4jListener> listeners) {
        this.logger = logger;
		this.listeners.addAll(listeners);
    }

    void addListener(Rdp4jListener listener) {
        listeners.add(listener);
    }

    void removeListener(Rdp4jListener listener) {
        listeners.remove(listener);
    }

    void beforePollingCycle(final BeforePollingCycleEvent event) throws InterruptedException {
        notifyListeners(PollCycleListener.class, listener -> listener.beforePollingCycle(event));
    }

    void afterPollingCycle(final AfterPollingCycleEvent event) throws InterruptedException {
        notifyListeners(PollCycleListener.class, listener -> listener.afterPollingCycle(event));
    }

    void fileAdded(final FileAddedEvent event) throws InterruptedException {
        notifyListeners(DirectoryListener.class, listener -> listener.fileAdded(event));
    }

    void fileRemoved(final FileRemovedEvent event) throws InterruptedException {
        notifyListeners(DirectoryListener.class, listener -> listener.fileRemoved(event));
    }

    void fileModified(final FileModifiedEvent event) throws InterruptedException {
        notifyListeners(DirectoryListener.class, listener -> listener.fileModified(event));
    }

    void ioErrorRaised(final IoErrorRaisedEvent event) throws InterruptedException {
        notifyListeners(IoErrorListener.class, listener -> listener.ioErrorRaised(event));
    }

    void ioErrorCeased(final IoErrorCeasedEvent event) throws InterruptedException {
        notifyListeners(IoErrorListener.class, listener -> listener.ioErrorCeased(event));
    }

    void afterStop(final AfterStopEvent event) {
        try {
            notifyListeners(DirectoryPollerListener.class, listener -> listener.afterStop(event));
        } catch (InterruptedException e) {
			Thread.currentThread().interrupt();
            // ignore
        }
    }

    void beforeStart(final BeforeStartEvent event) {
        try {
            notifyListeners(DirectoryPollerListener.class, listener -> listener.beforeStart(event));
        } catch (InterruptedException e) {
			Thread.currentThread().interrupt();
            // ignore
        }
    }

    void initialContent(final InitialContentEvent event) throws InterruptedException {
        notifyListeners(InitialContentListener.class, listener -> listener.initialContent(event));
    }

    private interface Notifier<T> {
        void notify(T listener) throws InterruptedException;
    }

    /*
     * Ignore if a listener crashes. Log on ERROR level and continue with next listener.
     * Don't let one listener ruin for other listeners...
     * if interrupted re-throw InterruptedException.
     */
    private <T extends Rdp4jListener> void notifyListeners(Class<T> listenerType, Notifier<T> notifier)
            throws InterruptedException {
        for (Rdp4jListener listener : listeners) {
            if (isInstanceOf(listener, listenerType)) {
                T listener2 = listenerType.cast(listener);
                try {
                    notifier.notify(listener2);
                } catch (InterruptedException e) {
                    throw e;
                } catch (Throwable e) {
                    logErrorMessage(e);
                }
            }
        }
    }

    <T extends Rdp4jListener> boolean isInstanceOf(Rdp4jListener listener, Class<T> listenerType) {
        return listenerType.isInstance(listener);
    }

    private void logErrorMessage(Throwable t) {
    	logger.error("Exception thrown by client implementation (of Rdp4jListener interface).", t);
    }

}
