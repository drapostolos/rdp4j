package com.github.drapostolos.rdp4j;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * This is a Skeletal implementation of all {@link Rdp4jListener} sub-interfaces. 
 * Contains default implementations of all methods.
 * <p>
 * It is up to the client to implement any desired methods.
 *
 */
public class AbstractRdp4jListener implements DirectoryListener, IoErrorListener, DirectoryPollerListener, PollCycleListener, InitialContentListener{
	private static final Logger logger = LoggerFactory.getLogger(AbstractRdp4jListener.class);

	/**
	 * Dummy implementation doing nothing.
	 */
	@Override
	public void beforeStart(BeforeStartEvent event) {
	}

	/**
	 * Dummy implementation doing nothing.
	 */
	@Override
	public void afterStop(AfterStopEvent event) {
	}

	/**
	 * Dummy implementation doing nothing.
	 */
	@Override
	public void beforePollingCycle(BeforePollingCycleEvent event) {
	}

	/**
	 * Dummy implementation doing nothing.
	 */
	@Override
	public void afterPollingCycle(AfterPollingCycleEvent event) {
	}

	/**
	 * Logs any occurred I/O errors
	 */
	@Override
	public void ioErrorRaised(IoErrorRaisedEvent event) {
		String message = "I/O error raised when polling directory '%s'!"; 
		PolledDirectory dir = event.getPolledDirectory();
		IOException e = event.getIoException();
		logger.error(String.format(message, dir), e);
	}

	/**
	 * Logs when an I/O error has ceased.
	 */
	@Override
	public void ioErrorCeased(IoErrorCeasedEvent event) {
		String message = "I/O error ceased when polling directory '%s'!"; 
		PolledDirectory dir = event.getPolledDirectory();
		logger.info(String.format(message, dir));
	}

	/**
	 * Dummy implementation doing nothing.
	 */
	@Override
	public void initialContent(InitialContentEvent event) {
	}

	/**
	 * Dummy implementation doing nothing.
	 */
	@Override
	public void fileAdded(FileAddedEvent event) {
	}

	/**
	 * Dummy implementation doing nothing.
	 */
	@Override
	public void fileRemoved(FileRemovedEvent event) {
	}

	/**
	 * Dummy implementation doing nothing.
	 */
	@Override
	public void fileModified(FileModifiedEvent event) {
	}
}
