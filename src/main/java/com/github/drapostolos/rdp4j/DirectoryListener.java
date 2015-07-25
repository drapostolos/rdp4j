package com.github.drapostolos.rdp4j;

import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * A listener of {@link FileAddedEvent} / {@link FileRemovedEvent} / {@link FileModifiedEvent}
 * events of the {@link DirectoryPoller}.
 * 
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 *
 */
public interface DirectoryListener extends Rdp4jListener{
	
    /**
     * Invoked each time a new file is added in the {@link PolledDirectory}.
     * 
     * @param event provided by the {@link DirectoryPoller}.
     * @throws InterruptedException when interrupted.
     */
    void fileAdded(FileAddedEvent event) throws InterruptedException;

	/**
     * Invoked each time a file is removed from the {@link PolledDirectory}.
     * 
     * @param event provided by the {@link DirectoryPoller}.
     * @throws InterruptedException when interrupted.
     */
    void fileRemoved(FileRemovedEvent event) throws InterruptedException;
	
	/**
     * Invoked each time a file in the {@link PolledDirectory} is modified.
     * 
     * @param event provided by the {@link DirectoryPoller}.
     * @throws InterruptedException when interrupted.
     */
    void fileModified(FileModifiedEvent event) throws InterruptedException;
}
