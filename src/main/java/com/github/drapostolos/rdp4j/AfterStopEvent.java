package com.github.drapostolos.rdp4j;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * An event that represents the stopping of the {@link DirectoryPoller}, as
 * triggered by this method: {@link DirectoryPoller#stop()}.
 * 
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public final class AfterStopEvent extends EventExposingDirectoryPoller {
    private Map<PolledDirectory, Set<FileElement>> currentFiles;
    private Map<PolledDirectory, Set<CachedFileElement>> currentCachedFiles;

	AfterStopEvent(DirectoryPoller directoryPoller, Set<Poller> pollers) {
        super(directoryPoller);
        currentCachedFiles = pollers.stream().collect(toMap(
        		p -> p.directory, 
        		p -> new HashSet<>(cachedFiles(p))));
        currentFiles = pollers.stream().collect(toMap(
        		p -> p.directory, 
        		p -> new HashSet<>(currentFiles(p))));
    }

	private Set<FileElement> currentFiles(Poller p) {
		return p.currentListedFiles.values().stream()
		.map(f -> f.getFileElement())
		.collect(toSet());
	}

	private Set<CachedFileElement> cachedFiles(Poller p) {
		return p.currentListedFiles.values().stream()
		.map(f -> f.getCachedFileElement())
		.collect(toSet());
	}

	/**
     * @return A Map with all {@link FileElement}s listed by each {@link PolledDirectory}
     * in the last poll.
	 */
	public Map<PolledDirectory, Set<FileElement>> getFileElements() {
		return currentFiles;
	}
	
	/**
     * @return A Map with all {@link CachedFileElement}s listed by each {@link PolledDirectory}
     * in the last poll.
	 */
	public Map<PolledDirectory, Set<CachedFileElement>> getCachedFileElements() {
		return currentCachedFiles;
	}
}
