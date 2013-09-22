package com.github.drapostolos.rdp4j;

import com.github.drapostolos.adp4j.spi.FileElement;
import com.github.drapostolos.adp4j.spi.PolledDirectory;

abstract class AbstractFileEvent extends AbstractDirectoryEvent{
	private final FileElement file;

	AbstractFileEvent(DirectoryPoller directoryPoller, PolledDirectory directory, FileElement file) {
		super(directoryPoller, directory);
		this.file = file;
	}
	
	/**
	 * 
	 * Return the {@link FileElement} triggering this event.
	 */
	public FileElement getFileElement() {
		return file;
	}
}
