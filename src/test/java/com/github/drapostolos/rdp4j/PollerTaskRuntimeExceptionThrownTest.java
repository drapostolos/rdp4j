package com.github.drapostolos.rdp4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Timer;

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
public class PollerTaskRuntimeExceptionThrownTest extends EventVerifier{

	@Test
	@PrepareForTest(LoggerFactory.class)
	public void runtimeExceptionThrown() throws Exception {
		// given fixture
		Logger loggerMock = Mockito.mock(Logger.class);
		PowerMockito.mockStatic(LoggerFactory.class);
		Mockito.when(LoggerFactory.getLogger(PollerTask.class)).thenReturn(loggerMock);
		PolledDirectory directory = Mockito.mock(PolledDirectory.class);
		listenerMock = Mockito.mock(AbstractRdp4jListener.class);
		inOrder = Mockito.inOrder(listenerMock);
		DirectoryPoller dp = Mockito.mock(DirectoryPoller.class);
		Mockito.when(dp.getDefaultFileFilter()).thenReturn(new DefaultFileFilter());
		dp.directories = new HashSet<PolledDirectory>(Arrays.asList(directory));
		dp.notifier = new ListenerNotifier(new HashSet<Rdp4jListener>(Arrays.asList(listenerMock)));
		dp.timer = Mockito.mock(Timer.class);
		pollerTask = new PollerTask(dp);
		
		Mockito.when(directory.listFiles())
		.thenReturn(list("fileA/1"))
		.thenThrow(new RuntimeException());

		// when
		executeNumberOfPollCycles(2);
		
		// then
		verifyEventsInOrder(
				// poll-cycle#1
				BeforePollingCycleEvent.class,
				InitialContentEvent.class,
				AfterPollingCycleEvent.class,
		
				// poll-cycle#2
				BeforePollingCycleEvent.class,
				AfterPollingCycleEvent.class
				);
		Mockito.verify(dp.timer).cancel();
		Mockito.verify(loggerMock).error(Mockito.anyString(), Mockito.any(Throwable.class));
		Mockito.verifyNoMoreInteractions(listenerMock);
	}
}
