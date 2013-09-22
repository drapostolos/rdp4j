package com.github.drapostolos.rdp4j;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.github.drapostolos.adp4j.core.DirectoryPoller;
import com.github.drapostolos.adp4j.core.DirectoryPollerBuilder;
import com.github.drapostolos.adp4j.spi.PolledDirectory;

public class DirectoryPollerTest {

	@Rule public ExpectedException expectedEx = ExpectedException.none();
	
	@Test
	public void shouldThrowExceptionWhenDirectoryNotSet() {
		expectedEx.expect(IllegalStateException.class);
		expectedEx.expectMessage(String.format("Unable to build the '%s'", DirectoryPoller.class.getSimpleName()));
		expectedEx.expectMessage(String.format("%s.addDirectory(PolledDirectory)", DirectoryPollerBuilder.class.getSimpleName()));
		DirectoryPoller.newBuilder().start();
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowExceptionWhenAddingDirectoryThatIsNull() {
		// given
		PolledDirectory directoryMock = Mockito.mock(PolledDirectory.class);
		DirectoryPoller dp = DirectoryPoller.newBuilder()
				.addPolledDirectory(directoryMock)
				.start();
		
		// when
		dp.addDirectory(null);
	}

	@Test(expected = NullPointerException.class)
	public void removeNullDirectory() {
		// given
		PolledDirectory directoryMock = Mockito.mock(PolledDirectory.class);
		DirectoryPoller dp = DirectoryPoller.newBuilder()
				.addPolledDirectory(directoryMock)
				.start();
		
		// when
		dp.removeDirectory(null);
	}

	@Test(expected = NullPointerException.class)
	public void addNullListener() {
		// given
		PolledDirectory directoryMock = Mockito.mock(PolledDirectory.class);
		DirectoryPoller dp = DirectoryPoller.newBuilder()
				.addPolledDirectory(directoryMock)
				.start();
		
		// when
		dp.addListener(null);
	}

	@Test(expected = NullPointerException.class)
	public void removeNullListener() {
		// given
		PolledDirectory directoryMock = Mockito.mock(PolledDirectory.class);
		DirectoryPoller dp = DirectoryPoller.newBuilder()
				.addPolledDirectory(directoryMock)
				.start();
		
		// when
		dp.removeListener(null);
	}

}
