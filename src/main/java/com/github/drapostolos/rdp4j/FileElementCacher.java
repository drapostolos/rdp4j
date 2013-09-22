package com.github.drapostolos.rdp4j;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.github.drapostolos.adp4j.spi.FileElement;

/**
 * This class exists solely for the purpose of holding cached 
 * attributes of {@link FileElement} instance. To avoid unnecessary
 * calls to a {@link FileElement} instance (in case it does live look ups).
 * <p>
 * It also performs utility operations on Maps with FileElementCacher
 */
class FileElementCacher {
	
	static Set<FileElement> toFileElements(Map<String, FileElementCacher> files){
		Set<FileElement> result = new LinkedHashSet<FileElement>();
		for(FileElementCacher file : files.values()){
			result.add(file.fileElement);
		}
		return result;
	}
	
	final FileElement fileElement;
	final String name;
	final long lastModified;

	FileElementCacher(FileElement fileElement, String name, long lastModified) {
		this.fileElement = fileElement;
		this.name = name;
		this.lastModified = lastModified;
	}

}
