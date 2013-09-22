package com.github.drapostolos.rdp4j;

import com.github.drapostolos.adp4j.spi.PolledDirectory;

/**
 * An event that represents the recovering of an I/O error in the {@link PolledDirectory}.
 *
 */
public final class IoErrorCeasedEvent extends AbstractDirectoryEvent {

	IoErrorCeasedEvent(DirectoryPoller dp, PolledDirectory directory) {
		super(dp, directory);
	}

}
