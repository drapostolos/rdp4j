package com.github.drapostolos.rdp4j;

import java.io.IOException;

import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * An event that represents the occurrence of an I/O error in the {@link PolledDirectory}.
 *
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public final class IoErrorRaisedEvent extends AbstractDirectoryEvent {
	private final IOException ioException;

	IoErrorRaisedEvent(DirectoryPoller dp, PolledDirectory directory, IOException e) {
		super(dp, directory);
		ioException = e;
	}
	
	/**
     * @return the {@link IOException} that caused this event.
     */
	public IOException getIoException() {
		return ioException;
	}

}
