package com.github.drapostolos.rdp4j.spi;

import java.io.IOException;

import com.github.drapostolos.rdp4j.DirectoryPoller;
import com.github.drapostolos.rdp4j.DirectoryPollerException;
import com.github.drapostolos.rdp4j.IoErrorListener;
import com.github.drapostolos.rdp4j.IoErrorRaisedEvent;

/**
 * Implementations of this interface represent a file element in the polled
 * directory.
 * 
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public interface FileElement {

	/**
	 * Returns the time when this {@link FileElement} was last modified
	 * (according to the remote file-system).
	 * <p>
	 * Returning a {@code 0L} value will be treated the same as if an 
	 * {@link IOException} was thrown.
	 * 
	 * @return A long value representing the time this {@link FileElement} 
	 * was last modified. 
	 * 
	 * @throws IOException if not possible to fetch the last modified time, due
	 * to I/O error. Throwing {@link IOException} will fire a {@link IoErrorRaisedEvent}
	 * event in {@link IoErrorListener#ioErrorRaised(IoErrorRaisedEvent)}.
	 * 
	 * @throws DirectoryPollerException For errors you don't want firing 
	 * {@link IoErrorRaisedEvent} events for. This will cause the Directory-Poller 
	 * to silently skip this poll-cycle and wait for next poll-cycle. In Other
	 * words use this exception when you think it is possible to recover within
	 * next coming poll-cycles (Keep a MaxRetries counter or similar in your implementation).
	 * 
	 * @throws Throwable if any unexpected crashes occurs. This will cause 
	 * the Directory-Poller to log an error message (along with the causing 
	 * {@link Throwable}) and stop the {@link DirectoryPoller}.
	 * 
	 */
	long lastModified() throws IOException;
	
	/**
	 * This method returns true, if this {@link FileElement} represents 
	 * a directory, otherwise false.
	 * <p>
	 * This method is optional to implement.
	 * 
	 * @return true if this {@link FileElement} instance represents a 
	 * directory, otherwise false.
	 */
	boolean isDirectory();
	
	/**
	 * Returns the name of this {@link FileElement}.
	 * <p>
	 * NOTE! </br>
	 * All files within a directory are expected to have unique names.
	 * 
	 * @return name of {@link FileElement}.
	 * 
	 * @throws DirectoryPollerException For errors you don't want firing 
	 * {@link IoErrorRaisedEvent} events for. This will cause the Directory-Poller 
	 * to silently skip this poll-cycle and wait for next poll-cycle. In Other
	 * words use this exception when you think it is possible to recover within
	 * next coming poll-cycles (Keep a MaxRetries counter or similar in your implementation).
	 * 
	 * @throws Throwable if any unexpected crashes occurs. This will cause 
	 * the Directory-Poller to log an error message (along with the causing 
	 * {@link Throwable}) and stop the {@link DirectoryPoller}.
	 */
	String getName();
}
