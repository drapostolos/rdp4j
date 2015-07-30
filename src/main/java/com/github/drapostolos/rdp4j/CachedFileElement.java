package com.github.drapostolos.rdp4j;

import com.github.drapostolos.rdp4j.spi.FileElement;

/**
 * This class exists solely for the purpose of holding cached
 * attributes of {@link FileElement} instance. To avoid unnecessary
 * calls to a {@link FileElement} instance (in case it does live look ups).
 */
class CachedFileElement implements FileElement {

    final FileElement fileElement;
    private final String name;
    private final long lastModified;

    CachedFileElement(FileElement fileElement, String name, long lastModified) {
        this.fileElement = fileElement;
        this.name = name;
        this.lastModified = lastModified;
    }

    @Override
    public long lastModified() {
        return lastModified;
    }

    @Override
    public boolean isDirectory() {
        return fileElement.isDirectory();
    }

    @Override
    public String getName() {
        return name;
    }

}
