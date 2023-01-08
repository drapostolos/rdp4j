package com.github.drapostolos.rdp4j;

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
	public void shouldThrowWhenMissingStorageFile() throws Exception {
		Assertions.assertThatThrownBy(() -> 
			new SerializeToFilePersister(missingFile, str -> null, dir -> null)
			.readData())
		.isInstanceOf(IllegalStateException.class)
		.hasMessageContaining("file.dat")
		.hasMessageContaining("missing-directory");
	}
	
	@Test
	public void canCreateMissingParentFoldersWhenPersisting() throws Exception {
		
		Assertions.assertThat(missingFile).doesNotExist();
		new SerializeToFilePersister(missingFile, str -> null, dir -> null)
		.writeData(new HashMap<>());
		
		Assertions.assertThat(missingFile).exists();
	}
}
