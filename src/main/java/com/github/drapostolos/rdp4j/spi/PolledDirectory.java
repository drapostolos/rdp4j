package com.github.drapostolos.rdp4j.spi;

import java.io.IOException;
import java.util.Set;

import com.github.drapostolos.rdp4j.DirectoryPoller;
import com.github.drapostolos.rdp4j.DirectoryPollerException;
import com.github.drapostolos.rdp4j.IoErrorListener;
import com.github.drapostolos.rdp4j.IoErrorRaisedEvent;

/**
 * Implementations of this interface represents the directory to poll for 
 * file elements.
 * 
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public interface PolledDirectory {
	
	    /**
     * Returns a snapshot of the current content in this directory, i.e.
     * listing all {@link FileElement}s in this directory.
     * <p>
     * Returning a {@code null} value will be treated the same as if an {@link IOException} was
     * thrown.
     * <p>
     * NOTE! </br> All files within a directory are expected to have unique names (i.e. method
     * {@link FileElement#getName()} is expected to return a name unique among all files within this
     * directory).
     * 
     * @return a list of {@link FileElement}s in this directory
     * @throws IOException if not possible to list files in this directory, due
     *         to I/O error. Throwing {@link IOException} will fire a {@link IoErrorRaisedEvent}
     *         event in {@link IoErrorListener#ioErrorRaised(IoErrorRaisedEvent)}.
     * @throws DirectoryPollerException For errors you don't want firing {@link IoErrorRaisedEvent}
     *         events for. This will cause the Directory-Poller
     *         to silently skip this poll-cycle and wait for next poll-cycle. In Other
     *         words use this exception when you think it is possible to recover within
     *         next coming poll-cycles (Keep a MaxRetries counter or similar in your
     *         implementation).
     * @throws RuntimeException if any unexpected crashes occurs. This will
     *         cause the Directory-Poller to log an error message (along with the causing
     *         {@link RuntimeException}) and stop the {@link DirectoryPoller}.
     */ 
	Set<FileElement> listFiles() throws IOException;
	
	/**
	 * It is recommended to implement this method if clients wants to remove this
	 * {@link PolledDirectory} from the {@link DirectoryPoller}.
	 * 
	 * @param obj
	 */
	@Override
	public boolean equals(Object obj);
	
	/**
	 * It is recommended to implement this method if clients wants to remove this
	 * {@link PolledDirectory} using {@link DirectoryPoller#removePolledDirectory(PolledDirectory)} .
	 */
	@Override
	public int hashCode();
}
