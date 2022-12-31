package com.github.drapostolos.rdp4j;

import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * An event that represents a modified file in the {@link PolledDirectory}.
 *
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public final class FileModifiedEvent extends EventExposingFileElement {

    FileModifiedEvent(DirectoryPoller directoryPoller, PolledDirectory directory, FileElementAndCache file) {
        super(directoryPoller, directory, file);
    }

}
