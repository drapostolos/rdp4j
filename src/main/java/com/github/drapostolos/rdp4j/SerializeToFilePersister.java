package com.github.drapostolos.rdp4j;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.notExists;
import static java.util.stream.Collectors.toMap;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.drapostolos.rdp4j.spi.Persister;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

class SerializeToFilePersister implements Persister {
	private static final Logger LOG = LoggerFactory.getLogger(SerializeToFilePersister.class);
	private Path persistedFile;
	private Function<String, PolledDirectory> stringToDirFunction;
	private Function<PolledDirectory, String> dirToStringFunction;

	SerializeToFilePersister(Path file, Function<String, PolledDirectory> stringToDir,
			Function<PolledDirectory, String> dirToString) {
				this.persistedFile = file;
				this.stringToDirFunction = stringToDir;
				this.dirToStringFunction = dirToString;
	}

	@Override
	public boolean containsData() {
		return Files.exists(persistedFile);
	}

	@Override
	public Map<PolledDirectory, Set<CachedFileElement>> readData() {
		try (	FileInputStream fis = new FileInputStream(persistedFile.toFile());
				ObjectInputStream ois = new ObjectInputStream(fis);){
			@SuppressWarnings("unchecked")
			Map<String, Set<CachedFileElement>> files = (Map<String, Set<CachedFileElement>>) ois.readObject();
			log(files, "Found persisted");
			return convertMapKey(files, stringToDirFunction);
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
		createParentDirectoryIfMissing(persistedFile);
		try (FileOutputStream fos = new FileOutputStream(persistedFile.toFile());
				ObjectOutputStream oos = new ObjectOutputStream(fos)) {
			oos.writeObject(convertMapKey(data, dirToStringFunction));
			oos.flush();
			log(data, "Persisting data");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private void createParentDirectoryIfMissing(Path storage) {
		Path parent = storage.getParent();
		if(parent != null) {
			if (notExists(parent)) {
				try {
					createDirectories(parent);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		}
	}

	private void log(Map<?, Set<CachedFileElement>> files, String subject) {
		LOG.info(subject + " [in " + persistedFile + "]");
		files.forEach((polledDirectory, fileElements) -> {
			LOG.info("  {} FileElements in {}", fileElements.size(), polledDirectory);
		});
	}
}
