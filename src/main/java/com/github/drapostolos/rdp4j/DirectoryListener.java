package com.github.drapostolos.rdp4j;

import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * A listener of {@link FileAddedEvent} / {@link FileRemovedEvent} / {@link FileModifiedEvent}
 * events of the {@link DirectoryPoller}.
 *
 */
public interface DirectoryListener extends Adp4jListener{
	/**
	 * Invoked each time a new file is added in the {@link PolledDirectory}.
	 * 
	 * @param event provided by the {@link DirectoryPoller}.
	 */
	void fileAdded(FileAddedEvent event);

	/**
	 * Invoked each time a file is removed from the {@link PolledDirectory}.
	 * 
	 * @param event provided by the {@link DirectoryPoller}.
	 */
	void fileRemoved(FileRemovedEvent event);
	
	/**
	 * Invoked each time a file in the {@link PolledDirectory} is modified.
	 * 
	 * @param event provided by the {@link DirectoryPoller}.
	 */
	void fileModified(FileModifiedEvent event);
}
