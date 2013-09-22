package com.github.drapostolos.rdp4j;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

public class AcceptanceTest extends EventVerifier {

	@Before
	public void testFixture() throws Exception {
		directoryMock = Mockito.mock(PolledDirectory.class);
		listenerMock = Mockito.mock(AbstractAdp4jListener.class);
		inOrder = Mockito.inOrder(listenerMock);
	}
	
	@Test
	public void enableParallelDirectoryPolling() throws Exception {
		// given 
		Mockito.when(directoryMock.listFiles())
		.thenReturn(list());
		
		// when
		DirectoryPoller dp = DirectoryPoller.newBuilder()
				.addPolledDirectory(directoryMock)
				.enableParallelPollingOfDirectories()
				.setPollingInterval(10, TimeUnit.MILLISECONDS)
				.start();
		dp.stop();
		
		// then
		Assertions.assertThat(dp.isParallelDirectoryPollingEnabled()).isTrue();
	}
	
	@Test
	public void defaultParallelDirectoryPolling() throws Exception {
		// given 
		Mockito.when(directoryMock.listFiles())
		.thenReturn(list());
		
		// when
		DirectoryPoller dp = DirectoryPoller.newBuilder()
				.addPolledDirectory(directoryMock)
				.start();
		dp.stop();
		
		// then
		Assertions.assertThat(dp.isParallelDirectoryPollingEnabled()).isFalse();
	}
	
	@Test
	public void enableFileAddedEventsForInitialContent() throws Exception {
		// given 
		Mockito.when(directoryMock.listFiles())
		.thenReturn(list());
		
		// when
		DirectoryPoller dp = DirectoryPoller.newBuilder()
				.addPolledDirectory(directoryMock)
				.enableFileAddedEventsForInitialContent()
				.setPollingInterval(10, TimeUnit.MILLISECONDS)
				.start();
		dp.stop();
		
		// then
		Assertions.assertThat(dp.isFileAdedEventForInitialContentEnabled()).isTrue();
	}
	
	@Test
	public void defaultFileAddedEventsForInitialContent() throws Exception {
		// given 
		Mockito.when(directoryMock.listFiles())
		.thenReturn(list());
		
		// when
		DirectoryPoller dp = DirectoryPoller.newBuilder()
				.addPolledDirectory(directoryMock)
				.start();
		dp.stop();
		
		// then
		Assertions.assertThat(dp.isFileAdedEventForInitialContentEnabled()).isFalse();
	}
	
	@Test
	public void fileAddedEventEnabledForInitialContent() throws Exception {
		// given 
		Mockito.when(directoryMock.listFiles())
		.thenReturn(list("file1.txt/1"));
		
		// when
		DirectoryPoller dp = DirectoryPoller.newBuilder()
				.addPolledDirectory(directoryMock)
				.addListener(listenerMock)
				.enableFileAddedEventsForInitialContent()
				.setPollingInterval(10, TimeUnit.MILLISECONDS)
				.start();
		TimeUnit.MILLISECONDS.sleep(50);
		dp.stop();
		
		// then
		verifyEventsInOrder(
				BeforeStartEvent.class,
				BeforePollingCycleEvent.class,
				FileAddedEvent.class,
				InitialContentEvent.class,
				AfterPollingCycleEvent.class,
				BeforePollingCycleEvent.class,
				AfterPollingCycleEvent.class,
				AfterStopEvent.class
				);
	}
	
	@Test
	public void addRemoveDirectories() throws Exception {
		// given 
		PolledDirectory directoryMock2 = Mockito.mock(PolledDirectory.class);
		
		// when
		DirectoryPoller dp = DirectoryPoller.newBuilder()
				.addPolledDirectory(directoryMock)
				.setPollingInterval(10, TimeUnit.MILLISECONDS)
				.start();
		dp.addDirectory(directoryMock2);
		
		// then 
		TimeUnit.MILLISECONDS.sleep(15); // to assure a polling cycle has passed
		Assertions.assertThat(dp.getPolledDirectories()).contains(directoryMock, directoryMock2);
		
		// when 
		dp.removeDirectory(directoryMock);
		dp.removeDirectory(directoryMock2);
		
		// then
		TimeUnit.MILLISECONDS.sleep(15); // to assure a polling cycle has passed
		Assertions.assertThat(dp.getPolledDirectories()).isEmpty();
		
		dp.stop();
	}
	
	@Test
	public void addListenerAfterStart() throws Exception {
		// given 
		Mockito.when(directoryMock.listFiles())
		.thenReturn(list())
		.thenReturn(list("file.txt/12"));
		
		// when
		DirectoryPoller dp = DirectoryPoller.newBuilder()
				.addPolledDirectory(directoryMock)
				.setPollingInterval(10, TimeUnit.MILLISECONDS)
				.start();
		dp.addListener(listenerMock);
		TimeUnit.MILLISECONDS.sleep(50);
		dp.stop();
		
		// then
		verifyEventsInOrder(
				BeforePollingCycleEvent.class,
				FileAddedEvent.class,
				AfterPollingCycleEvent.class,
				AfterStopEvent.class
				);
	}
	
	@Test
	public void removeListener() throws Exception {
		// given 
		Mockito.when(directoryMock.listFiles())
		.thenReturn(list())
		.thenReturn(list("file.txt/12"));
		
		// when
		DirectoryPoller dp = DirectoryPoller.newBuilder()
				.addPolledDirectory(directoryMock)
				.addListener(listenerMock)
				.setPollingInterval(10, TimeUnit.MILLISECONDS)
				.start();
		TimeUnit.MILLISECONDS.sleep(10);
		dp.removeListener(listenerMock);
		TimeUnit.MILLISECONDS.sleep(20);
		dp.stop();
		
		Mockito.verify(listenerMock, Mockito.times(0)).afterStop(Mockito.any(AfterStopEvent.class));
	}
	
	@Test
	public void toStringValue() throws Exception {
		// given 
		Mockito.when(directoryMock.listFiles())
		.thenReturn(list());
		
		// when
		DirectoryPoller dp = DirectoryPoller.newBuilder()
				.addPolledDirectory(directoryMock)
				.start();
		dp.stop();
		
		Assertions.assertThat(dp.toString()).matches("DirectoryPoller-\\d+: .*\\[polling every: 1000 milliseconds\\]");
		
	}
	
	@Test
	public void OneSuccesfulPoll() throws Exception {
		// given 
		Mockito.when(directoryMock.listFiles())
		.thenReturn(list());
		
		// when
		DirectoryPoller dp = DirectoryPoller.newBuilder()
				.addListener(listenerMock)
				.addPolledDirectory(directoryMock)
				.setPollingInterval(200, TimeUnit.MILLISECONDS)
				.start();
		TimeUnit.MILLISECONDS.sleep(10);
		dp.stop();
		
		// then
		Assertions.assertThat(dp.getThreadName()).matches("DirectoryPoller-\\d+");
		verifyEventsInOrder(
				BeforeStartEvent.class,
				BeforePollingCycleEvent.class,
				InitialContentEvent.class,
				AfterPollingCycleEvent.class,
				AfterStopEvent.class
				);
		Mockito.verifyNoMoreInteractions(listenerMock);
	}
	
	@Test
	public void OneSuccesfulPollUsingFileFilter() throws Exception {
		// given 
		Mockito.when(directoryMock.listFiles())
		.thenReturn(list("a.txt/12", "b.xml/11"));
		
		final Set<FileElement> files = new LinkedHashSet<FileElement>();
		Mockito.doAnswer(new Answer<InitialContentEvent>() {
			@Override
			public InitialContentEvent answer(InvocationOnMock invocation) throws Throwable {
				Set<FileElement> s = ((InitialContentEvent) invocation.getArguments()[0]).getFiles();
				files.addAll(s);
				return null;
			}
		}).when(listenerMock).initialContent(Mockito.any(InitialContentEvent.class));

		// when
		DirectoryPoller dp = DirectoryPoller.newBuilder()
				.addListener(listenerMock)
				.addPolledDirectory(directoryMock)
				.setDefaultFileFilter(new RegexFileFilter(".*\\.txt"))
				.setThreadName("NAME")
				.setPollingInterval(200, TimeUnit.MILLISECONDS)
				.start();
		TimeUnit.MILLISECONDS.sleep(10);
		dp.stop();
		
		// then
		Assertions.assertThat(files).isEqualTo(list("a.txt/12"));
		Assertions.assertThat(dp.getThreadName()).isEqualTo("NAME");
		Assertions.assertThat(dp.getPollingIntervalInMillis()).isEqualTo(200);
		verifyEventsInOrder(
				BeforeStartEvent.class,
				BeforePollingCycleEvent.class,
				InitialContentEvent.class,
				AfterPollingCycleEvent.class,
				AfterStopEvent.class
				);
		Mockito.verifyNoMoreInteractions(listenerMock);
	}
}
