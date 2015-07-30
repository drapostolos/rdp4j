package com.github.drapostolos.rdp4j;

import static com.github.drapostolos.rdp4j.Util.copyValuesToFileElementSet;

import java.util.Map;
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

    private final Set<FileElement> copy;

    InitialContentEvent(DirectoryPoller dp, PolledDirectory directory, Map<String, CachedFileElement> initialFiles) {
        super(dp, directory);
        copy = copyValuesToFileElementSet(initialFiles);
    }

    /**
     * @return a set of all {@link FileElement}s contained in a {@link PolledDirectory}
     *         at startup.
     */
    // TODO return the CachedFileElement instead? fix this when fixing :
    // https://github.com/drapostolos/rdp4j/issues/2
    public Set<FileElement> getFiles() {
        return copy;
    }

}
