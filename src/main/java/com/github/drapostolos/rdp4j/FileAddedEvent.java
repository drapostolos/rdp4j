package com.github.drapostolos.rdp4j;

import com.github.drapostolos.adp4j.spi.FileElement;
import com.github.drapostolos.adp4j.spi.PolledDirectory;

/**
 * An event that represents a new file added in the {@link PolledDirectory}.
 *
 */
public final class FileAddedEvent extends AbstractFileEvent{

	FileAddedEvent(DirectoryPoller directoryPoller, PolledDirectory directory, FileElement file) {
		super(directoryPoller, directory, file);
	}

}
