package com.github.drapostolos.rdp4j;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.drapostolos.adp4j.core.AbstractAdp4jListener;
import com.github.drapostolos.adp4j.core.BeforeStartEvent;
import com.github.drapostolos.adp4j.core.DirectoryPoller;
import com.github.drapostolos.adp4j.core.ListenerNotifier;
import com.github.drapostolos.adp4j.spi.PolledDirectory;

@RunWith(PowerMockRunner.class)
public class ListenerThrowsExceptionTest {

	@Test
	@PrepareForTest(LoggerFactory.class)
	public void listenerThrowsException() throws Exception {

		// given
		Logger loggerMock = Mockito.mock(Logger.class);
		PowerMockito.mockStatic(LoggerFactory.class);
		Mockito.when(LoggerFactory.getLogger(ListenerNotifier.class)).thenReturn(loggerMock);
		AbstractAdp4jListener listenerMock = Mockito.mock(AbstractAdp4jListener.class);
		Mockito.doThrow(RuntimeException.class).when(listenerMock).beforeStart(Mockito.any(BeforeStartEvent.class));
		PolledDirectory directoryMock = Mockito.mock(PolledDirectory.class);

		// when
		DirectoryPoller.newBuilder()
		.addListener(listenerMock)
		.addPolledDirectory(directoryMock)
		.start();

		// then
		Mockito.verify(loggerMock).error(Mockito.anyString(), Mockito.any(Throwable.class));
	}
}