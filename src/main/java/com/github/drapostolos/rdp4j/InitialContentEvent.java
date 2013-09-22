package com.github.drapostolos.rdp4j;

import java.util.HashSet;
import java.util.Set;

import com.github.drapostolos.adp4j.spi.FileElement;
import com.github.drapostolos.adp4j.spi.PolledDirectory;

/**
 * An event that provides the initial content of the {@link PolledDirectory}.
 * <p>
 * The initial content of a directory are the files/directories 
 * it contains the first poll-cycle.

 *
 */
public final class InitialContentEvent extends AbstractDirectoryEvent{
	private final Set<FileElement> files;

	InitialContentEvent(DirectoryPoller dp, PolledDirectory directory, Set<FileElement> files) {
		super(dp, directory);
		this.files = files;
	}

	/**
	 * Returns a set of all {@link FileElement}s contained in a {@link PolledDirectory}
	 * at startup.
	 */
	public Set<FileElement> getFiles(){
		return new HashSet<FileElement>(files);
	}

}
