package com.github.drapostolos.rdp4j;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

abstract class EventExposingFileElement extends EventExposingPolledDirectory {
    private final FileElementAndCache cache;

    EventExposingFileElement(DirectoryPoller directoryPoller, PolledDirectory directory, FileElementAndCache cache) {
        super(directoryPoller, directory);
        this.cache = cache;
    }

    /**
     * @return the {@link FileElement} triggering this event.
     */
    public FileElement getFileElement() {
        return cache.getFileElement();
    }

    /**
     * @return cached version of the {@link FileElement} that triggering this event.
     * 
     * A cached {@link FileElement} will always return the same values, for example {@link FileElement#lastModified()}
     * will return the value used by the {@link DirectoryPoller} during this poll cycle. Any modifications to
     * the real {@link FileElement} will not be reflected in {@link CachedFileElement}. 
     */
    public CachedFileElement getCachedFileElement() {
        return cache.getCachedFileElement();
    }
}
