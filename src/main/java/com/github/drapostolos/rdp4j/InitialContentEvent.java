package com.github.drapostolos.rdp4j;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Set;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * An event that provides the initial content of the {@link PolledDirectory}.
 * <p>
 * The initial content of a directory are the files/directories
 * it contains the first poll-cycle.
 *
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public final class InitialContentEvent extends EventExposingPolledDirectory {
    private final Set<FileElement> fileElements;
    private final Set<CachedFileElement> cachedFileElements;

    InitialContentEvent(DirectoryPoller dp, PolledDirectory directory, Collection<FileElementAndCache> files) {
        super(dp, directory);
        fileElements = files.stream()
        		.map(FileElementAndCache::getFileElement)
        		.collect(toSet());
        cachedFileElements = files.stream()
        		.map(FileElementAndCache::getCachedFileElement)
        		.collect(toSet());
    }
    
	/**
	 * Use {@link #getFileElements()} method instead.
	 * 
     * @return a set of all {@link FileElement}s contained in this {@link PolledDirectory}
     *         at startup.
     */
    @Deprecated
    public Set<FileElement> getFiles() {
        return getFileElements();
    }

	/**
     * @return a set of all {@link FileElement}s contained in this {@link PolledDirectory}
     *         at startup.
     */
    public Set<FileElement> getFileElements() {
        return fileElements;
    }

    /**
     * @return a set of all {@link CachedFileElement}s contained in this {@link PolledDirectory}
     *         at startup.
     */
    public Set<CachedFileElement> getCachedFileElements() {
        return cachedFileElements;
    }
}
