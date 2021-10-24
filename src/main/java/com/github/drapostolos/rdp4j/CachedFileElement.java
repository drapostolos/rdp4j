package com.github.drapostolos.rdp4j;

import java.io.IOException;
import java.io.Serializable;

import com.github.drapostolos.rdp4j.spi.FileElement;

/**
 * This class exists solely for the purpose of holding serializable cached
 * attributes of a {@link FileElement} instance. To avoid unnecessary
 * calls to a {@link FileElement} instance (in case it does live look ups).
 */
public final class CachedFileElement implements FileElement, Serializable {
	private static final long serialVersionUID = 1L;
	private final String name;
    private final long lastModified;
    private final boolean isDirectory;
    
	static CachedFileElement of(FileElement fileElement) throws IOException {
		return of(fileElement.getName(), fileElement.lastModified(), fileElement.isDirectory());
	}

	/**
	 * Returns a {@link FileElement} implementation for a file that holds 
	 * cached data, i.e. a snapshot of a files state at some point in time. 
	 * No live lookup is done.
	 * 
	 * @param name of the file.
	 * @param timeModified the time this file was modified.
	 * @return a {@link FileElement} with cached values.
	 */
	public static CachedFileElement ofFile(String name, long timeModified) {
		return of(name, timeModified, false);
	}

	/**
	 * Returns a {@link FileElement} implementation for a directory that holds 
	 * cached data, i.e. a snapshot of a files state at some point in time. 
	 * No live lookup is done.
	 * 
	 * @param name of the file.
	 * @param timeModified the time this directory was modified.
	 * @return a {@link FileElement} with cached values.
	 */
	public static CachedFileElement ofDir(String name, long timeModified) {
		return of(name, timeModified, true);
	}

	/**
	 * Returns a {@link FileElement} implementation that holds cached data, i.e. a snapshot
	 * of a files state at some point in time. No live lookup is done.
	 * 
	 * @param name of the file.
	 * @param timeModified the time this file was modified.
	 * @param isDirectory true if this is a directory, otherwise false.
	 * @return a {@link FileElement} with cached values.
	 */
	public static CachedFileElement of(String name, long timeModified, boolean isDirectory) {
		return new CachedFileElement(name, timeModified, isDirectory);
	}

	private CachedFileElement(String name, long lastModified, boolean isDirectory) {
        this.name = name;
        this.lastModified = lastModified;
		this.isDirectory = isDirectory;
    }

    @Override
    public long lastModified() {
        return lastModified;
    }

    @Override
    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public String getName() {
        return name;
    }
    
	@Override
	public String toString() {
		return "CachedFileElement [name=" + name + ", lastModified=" + lastModified + ", isDirectory=" + isDirectory
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (lastModified ^ (lastModified >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CachedFileElement other = (CachedFileElement) obj;
		if (lastModified != other.lastModified)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
