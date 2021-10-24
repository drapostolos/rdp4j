package com.github.drapostolos.rdp4j;

import com.github.drapostolos.rdp4j.spi.FileElement;

final class FileElementAndCache {
	private final FileElement file;
	private final CachedFileElement cache;
	

	FileElementAndCache(FileElement file, CachedFileElement cache) {
		this.file = file;
		this.cache = cache;
		
	}
	
	FileElement getFileElement() {
		return file;
	}

	CachedFileElement getCachedFileElement() {
		return cache;
	}

	public long lastModified() {
		return cache.lastModified();
	}

	public String getName() {
		return cache.getName();
	}
	
	@Override
	public String toString() {
		return String.format("FileElementAndCache [%s;%s;%s]", 
				cache.getName(), cache.lastModified(), cache.isDirectory());
	}
}
