package com.github.drapostolos.rdp4j;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;

public class SerializeToFilePersisterTest {
	
	private Path missingFile = Paths.get("missing-directory", "file.dat").toAbsolutePath();

	@After
	public void teardown() throws Exception {
		System.out.println("AFTER");
		Files.deleteIfExists(missingFile);
		System.out.println("exists: " + Files.exists(missingFile));
		Files.deleteIfExists(missingFile.getParent());
	}
	
	@Test
	public void shouldThrowWhenReadingMissingPersistedFile() throws Exception {
		Assertions.assertThatThrownBy(() -> 
			new SerializeToFilePersister(missingFile, str -> null, dir -> null)
			.readData())
		.isInstanceOf(IllegalStateException.class)
		.hasMessageContaining("file.dat")
		.hasMessageContaining("missing-directory");
	}
	
	@Test
	public void canCreatePersistingFileWhenParentFolderExists() throws Exception {
		// Given
		Assertions.assertThat(missingFile).doesNotExist();
		Files.createDirectories(missingFile.getParent());
		
		// When
		new SerializeToFilePersister(missingFile, str -> null, dir -> null)
		.writeData(new HashMap<>());
		
		// Then
		Assertions.assertThat(missingFile).exists();
	}
	
	@Test
	public void shouldThrowIfPersistedFileIsDirectory() throws Exception {
		// Given
		Files.createDirectories(missingFile.getParent());
		
		// Then
		Assertions.assertThatThrownBy(() -> 
			new SerializeToFilePersister(missingFile.getParent(), str -> null, dir -> null)
			.writeData(new HashMap<>()))
		.isInstanceOf(IllegalStateException.class)
		.hasCauseInstanceOf(FileNotFoundException.class)
		.hasMessageContaining("missing-directory");
	}
	
	@Test
	public void canCreateMissingParentFoldersWhenPersisting() throws Exception {
		// Given
		Assertions.assertThat(missingFile).doesNotExist();
		
		// When
		new SerializeToFilePersister(missingFile, str -> null, dir -> null)
		.writeData(new HashMap<>());
		
		// Then
		Assertions.assertThat(missingFile).exists();
	}
}
