package com.github.drapostolos.rdp4j;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * An event that represents a removed file in the {@link PolledDirectory}.
 *
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public final class FileRemovedEvent extends AbstractFileEvent {

	FileRemovedEvent(DirectoryPoller directoryPoller, PolledDirectory directory, FileElement file) {
		super(directoryPoller, directory, file);
	}


}
