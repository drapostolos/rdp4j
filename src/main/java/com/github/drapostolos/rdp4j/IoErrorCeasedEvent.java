package com.github.drapostolos.rdp4j;

import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * An event that represents the recovering of an I/O error in the {@link PolledDirectory}.
 *
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public final class IoErrorCeasedEvent extends AbstractDirectoryEvent {

	IoErrorCeasedEvent(DirectoryPoller dp, PolledDirectory directory) {
		super(dp, directory);
	}

}
