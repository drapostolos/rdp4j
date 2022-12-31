package com.github.drapostolos.rdp4j.spi;

import java.util.Map;
import java.util.Set;

import com.github.drapostolos.rdp4j.CachedFileElement;
import com.github.drapostolos.rdp4j.DirectoryPoller;

/**
 * Implementations of this interface handles persisting of {@link DirectoryPoller}
 * and {@link CachedFileElement}. Both writing/reading data from a persisting source.
 *  
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public interface Persister {

	/**
	 * @return true if there is persisted data to read, otherwise false.
	 */
	boolean containsData();
	
	/**
	 * Reads persisted data from a source.
	 * 
	 * @return the persisted data
	 */
	Map<PolledDirectory, Set<CachedFileElement>> readData();

	/**
	 * Persists the given <code>data</code>.
	 * 
	 * @param data the data to persist.
	 */
	void writeData(Map<PolledDirectory, Set<CachedFileElement>> data);
}
