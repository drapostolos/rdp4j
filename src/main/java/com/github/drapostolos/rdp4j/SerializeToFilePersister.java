package com.github.drapostolos.rdp4j;

import static java.util.stream.Collectors.toMap;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import com.github.drapostolos.rdp4j.spi.Persister;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

class SerializeToFilePersister implements Persister {
	private Path storage;
	private Function<String, PolledDirectory> stringToDir;
	private Function<PolledDirectory, String> dirToString;

	SerializeToFilePersister(Path file, Function<String, PolledDirectory> stringToDir,
			Function<PolledDirectory, String> dirToString) {
				this.storage = file;
				this.stringToDir = stringToDir;
				this.dirToString = dirToString;
	}

	@Override
	public boolean containsData() {
		return Files.exists(storage);
	}

	@Override
	public Map<PolledDirectory, Set<CachedFileElement>> readData() {
		try (	FileInputStream fis = new FileInputStream(storage.toFile());
				ObjectInputStream ois = new ObjectInputStream(fis);){
		    @SuppressWarnings("unchecked")
			Map<String, Set<CachedFileElement>> files = (Map<String, Set<CachedFileElement>>) ois.readObject();
		    return convertMapKey(files, stringToDir);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private <T, S> Map<T, Set<CachedFileElement>> convertMapKey(
			Map<S, Set<CachedFileElement>> map, Function<S, T> mapper) {
		return map.entrySet().stream()
		.collect(toMap(e -> mapper.apply(e.getKey()), Entry::getValue));
	}
	
	@Override
	public void writeData(Map<PolledDirectory, Set<CachedFileElement>> data) {
		try (FileOutputStream fos = new FileOutputStream(storage.toFile());
				ObjectOutputStream oos = new ObjectOutputStream(fos)) {
			oos.writeObject(convertMapKey(data, dirToString));
			oos.flush();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
