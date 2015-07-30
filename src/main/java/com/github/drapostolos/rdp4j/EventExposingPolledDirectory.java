package com.github.drapostolos.rdp4j;

import com.github.drapostolos.rdp4j.spi.PolledDirectory;

abstract class EventExposingPolledDirectory extends EventExposingDirectoryPoller {

    private final PolledDirectory directory;

    EventExposingPolledDirectory(DirectoryPoller directoryPoller, PolledDirectory directory) {
        super(directoryPoller);
        this.directory = directory;
    }

    /**
     * @return The {@link PolledDirectory} instance, where this event occurred.
     */
    public PolledDirectory getPolledDirectory() {
        return directory;
    }

}
