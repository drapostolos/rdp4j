package com.github.drapostolos.rdp4j;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * An event that represents a new file added in the {@link PolledDirectory}.
 *
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public final class FileAddedEvent extends EventExposingFileElement{

	FileAddedEvent(DirectoryPoller directoryPoller, PolledDirectory directory, FileElement file) {
		super(directoryPoller, directory, file);
	}

}
