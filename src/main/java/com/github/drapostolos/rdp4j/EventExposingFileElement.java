package com.github.drapostolos.rdp4j;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

abstract class EventExposingFileElement extends EventExposingPolledDirectory{
	private final FileElement file;

	EventExposingFileElement(DirectoryPoller directoryPoller, PolledDirectory directory, FileElement file) {
		super(directoryPoller, directory);
		this.file = file;
	}
	
	/**
     * @return the {@link FileElement} triggering this event.
     */
	public FileElement getFileElement() {
		return file;
	}
}
