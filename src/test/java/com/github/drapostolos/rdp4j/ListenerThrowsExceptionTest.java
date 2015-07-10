package com.github.drapostolos.rdp4j;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.drapostolos.rdp4j.spi.PolledDirectory;

@RunWith(PowerMockRunner.class)
public class ListenerThrowsExceptionTest {

	@Test
	@PrepareForTest(LoggerFactory.class)
	public void listenerThrowsException() throws Exception {

		// given
		Logger loggerMock = Mockito.mock(Logger.class);
		PowerMockito.mockStatic(LoggerFactory.class);
		Mockito.when(LoggerFactory.getLogger(ListenerNotifier.class)).thenReturn(loggerMock);
		AbstractRdp4jListener listenerMock = Mockito.mock(AbstractRdp4jListener.class);
		Mockito.doThrow(RuntimeException.class).when(listenerMock).beforeStart(Mockito.any(BeforeStartEvent.class));
		PolledDirectory directoryMock = Mockito.mock(PolledDirectory.class);

		// when
        DirectoryPoller dp = DirectoryPoller.newBuilder()
		.addListener(listenerMock)
		.addPolledDirectory(directoryMock)
		.start();

        dp.stop();

		// then
		Mockito.verify(loggerMock).error(Mockito.anyString(), Mockito.any(Throwable.class));
	}
}