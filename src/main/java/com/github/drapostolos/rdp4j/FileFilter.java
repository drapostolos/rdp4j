package com.github.drapostolos.rdp4j;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;


/**
 * A filter applied when listing {@link FileElement}s in a {@link PolledDirectory}.
 * Only {@link FileElement} satisfying this filter will be considered.
 */
public interface FileFilter {

	/**
	 * Return true if the given <code>file</code> is accepted by this filter,
	 * otherwise returns false.
	 * @param file 
	 */
    boolean accept(FileElement file);

}
