package com.github.drapostolos.rdp4j.spi;

import java.util.Map;
import java.util.Set;

import com.github.drapostolos.rdp4j.CachedFileElement;

public interface Persister {
	
	void write(Map<PolledDirectory, Set<CachedFileElement>> data);

	Map<PolledDirectory, Set<CachedFileElement>> read();
	
}
