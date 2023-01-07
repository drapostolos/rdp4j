package com.github.drapostolos.rdp4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

public class ScheduledRunnableTest extends EventVerifier {
	
	@Mock
	Logger notifierLogger;

    @Before
    public void testFixture() throws Exception {
        directoryMock = Mockito.mock(PolledDirectory.class);
        listenerMock = Mockito.mock(AbstractRdp4jListener.class);
        inOrder = Mockito.inOrder(listenerMock);
        directoryPollerMock = Mockito.mock(DirectoryPoller.class);
        Mockito.when(directoryPollerMock.getDefaultFileFilter()).thenReturn(new DefaultFileFilter());
        directories.put(directoryMock, new HashSet<>());
        directoryPollerMock.directories = directories;
        directoryPollerMock.notifier = new ListenerNotifier(notifierLogger, new HashSet<Rdp4jListener>(Arrays.asList(listenerMock)));
        pollerTask = new ScheduledRunnable(directoryPollerMock);
    }

    @After
    public void cleanup() throws Exception {
        if (pollerTask != null) {
            pollerTask.shutdown();
            pollerTask.awaitTermination();
        }
    }
    
    @Test
	public void defaultLoggerCalledWhenIoErrorRaisedOrCeased() throws Exception {
        // given 
    	Logger ioErrorLogger = Mockito.mock(Logger.class);
        Mockito.when(directoryMock.listFiles())
                .thenThrow(new IOException("thrown from unit test!"))
                .thenReturn(list("fileA/1", "fileB/1"))
                ;
        CountDownLatch secondPollCycle = new CountDownLatch(2);
        
        // When
		DirectoryPoller dp = DirectoryPoller.newBuilder()
		.addPolledDirectory(directoryMock)
		.addListener(new AbstractRdp4jListener(ioErrorLogger) {
			@Override
			public void afterPollingCycle(AfterPollingCycleEvent event) throws InterruptedException {
				secondPollCycle.countDown();
			}
		})
		.start();
		secondPollCycle.await();
		dp.stop();
		
		// then
		Mockito.verify(ioErrorLogger).error(contains("I/O error raised"), (IOException) any());
		Mockito.verify(ioErrorLogger).info(contains("I/O error ceased"));
	}

    @Test
    public void filterOutAFile() throws Exception {
        // given 
        // This will change the filter used by the PollerTask
        Mockito.when(directoryPollerMock.getDefaultFileFilter()).thenReturn(new RegexFileFilter("file[AB]"));
        pollerTask = new ScheduledRunnable(directoryPollerMock);

        Mockito.when(directoryMock.listFiles())
                .thenReturn(list("fileA/1"))
                .thenReturn(list("fileA/1", "fileB/1"))
                .thenReturn(list("fileA/1", "fileB/1", "fileC/1"));

        // when
        executeNumberOfPollCycles(3);
        // then
        verifyEventsInOrder(
                // poll-cycle#1
                BeforePollingCycleEvent.class,
                InitialContentEvent.class,
                AfterPollingCycleEvent.class,

                // poll-cycle#2
                BeforePollingCycleEvent.class,
                FileAddedEvent.class,
                AfterPollingCycleEvent.class,

                // poll-cycle#3
                BeforePollingCycleEvent.class,
                AfterPollingCycleEvent.class);
        Mockito.verifyNoMoreInteractions(listenerMock);
    }

    @Test
    public void addRemoveListeners() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list("fileA/1"))
                .thenReturn(list("fileA/1", "fileB/1"))
                .thenReturn(list("fileA/1", "fileB/1", "fileC/1"));

        // when
        pollerTask.run(); // poll-cycle 1
        pollerTask.removeListener(listenerMock);
        pollerTask.run(); // poll-cycle 2
        pollerTask.addListener(listenerMock);
        pollerTask.run(); // poll-cycle 3

        // then
        verifyEventsInOrder(
                // poll-cycle#1
                BeforePollingCycleEvent.class,
                InitialContentEvent.class,
                AfterPollingCycleEvent.class,

                // poll-cycle#2
                // Nothing should happen

                // poll-cycle#3
                BeforePollingCycleEvent.class,
                FileAddedEvent.class,
                AfterPollingCycleEvent.class);
        Mockito.verifyNoMoreInteractions(listenerMock);
    }

    @Test
    public void addRemoveDirectories() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list("fileA/1"))
                .thenReturn(list("fileA/1", "fileB/1"));

        PolledDirectory directoryMock2 = Mockito.mock(PolledDirectory.class);
        Mockito.when(directoryMock2.listFiles())
                .thenReturn(list("FileA/1"))
                .thenReturn(list("FileA/1", "FileB/1"))
                .thenReturn(list("FileA/1", "FileB/1", "FileC/1"));

        // when
        pollerTask.run(); // poll-cycle 1
        pollerTask.addDirectory(directoryMock2);
        pollerTask.run(); // poll-cycle 2
        pollerTask.removeDirectory(directoryMock);
        pollerTask.removeDirectory(directoryMock2);
        pollerTask.run(); // poll-cycle 3
        pollerTask.addDirectory(directoryMock2);
        pollerTask.run(); // poll-cycle 4
        pollerTask.run(); // poll-cycle 5

        // then
        verifyEventsInOrder(
                // poll-cycle#1
                BeforePollingCycleEvent.class,
                InitialContentEvent.class, // fileA/1
                AfterPollingCycleEvent.class,

                // poll-cycle#2
                BeforePollingCycleEvent.class,
                FileAddedEvent.class, // fileB/1
                InitialContentEvent.class, // FileA/1
                AfterPollingCycleEvent.class,

                // poll-cycle#3
                BeforePollingCycleEvent.class,
                AfterPollingCycleEvent.class,

                // poll-cycle#4
                BeforePollingCycleEvent.class,
                InitialContentEvent.class, // FileA/1, FileB/1
                AfterPollingCycleEvent.class,

                // poll-cycle#5
                BeforePollingCycleEvent.class,
                FileAddedEvent.class, // FileC/1
                AfterPollingCycleEvent.class);
        Mockito.verifyNoMoreInteractions(listenerMock);
    }

    @Test
    public void oneSuccesfulPoll() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list());

        // when
        executeNumberOfPollCycles(1);

        // then
        verifyEventsInOrder(
                BeforePollingCycleEvent.class,
                InitialContentEvent.class,
                AfterPollingCycleEvent.class);
        Mockito.verifyNoMoreInteractions(listenerMock);
    }

    @Test
    public void initiallyIoErrorRaisedThenCeased() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenThrow(new IOException("thrown from unit test!"))
                .thenReturn(null)
                .thenReturn(list("fileA/1", "fileB/1"));

        // when
        executeNumberOfPollCycles(3);

        // then
        verifyEventsInOrder(
                // poll-cycle#1
                BeforePollingCycleEvent.class,
                IoErrorRaisedEvent.class,
                AfterPollingCycleEvent.class,

                // poll-cycle#2
                BeforePollingCycleEvent.class,
                AfterPollingCycleEvent.class,

                // poll-cycle#3
                BeforePollingCycleEvent.class,
                IoErrorCeasedEvent.class,
                InitialContentEvent.class,
                AfterPollingCycleEvent.class);
        Mockito.verifyNoMoreInteractions(listenerMock);
    }

    @Test
    public void IoErrorRaisedByLastModifiedThenCeased() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list("fileA/1", "fileB/1"))
                .thenReturn(list("fileA/0", "fileB/1"))
                .thenReturn(list("fileA/1", "fileB/1"));

        // when
        executeNumberOfPollCycles(3);

        // then
        verifyEventsInOrder(
                // poll-cycle#1
                BeforePollingCycleEvent.class,
                InitialContentEvent.class,
                AfterPollingCycleEvent.class,

                // poll-cycle#2
                BeforePollingCycleEvent.class,
                IoErrorRaisedEvent.class,
                AfterPollingCycleEvent.class,

                // poll-cycle#3
                BeforePollingCycleEvent.class,
                IoErrorCeasedEvent.class,
                AfterPollingCycleEvent.class);
        Mockito.verifyNoMoreInteractions(listenerMock);
    }

    @Test
    public void directoryPollerExceptionThrown() throws Exception {
        // given
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list("fileA/1"))
                .thenThrow(new DirectoryPollerException());

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
                AfterPollingCycleEvent.class);
        Mockito.verifyNoMoreInteractions(listenerMock);
    }

    @Test
    public void addRemoveModifyFiles() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list("fileA/1"))
                .thenReturn(list("fileA/1", "fileB/1"))
                .thenReturn(list("fileA/2", "fileC/1"));

        // when
        executeNumberOfPollCycles(4);

        // then
        verifyEventsInOrder(
                // poll-cycle#1
                BeforePollingCycleEvent.class,
                InitialContentEvent.class,
                AfterPollingCycleEvent.class,

                // poll-cycle#2
                BeforePollingCycleEvent.class,
                FileAddedEvent.class,
                AfterPollingCycleEvent.class,

                // poll-cycle#3
                BeforePollingCycleEvent.class,
                FileRemovedEvent.class,
                FileAddedEvent.class,
                FileModifiedEvent.class,
                AfterPollingCycleEvent.class,

                // poll-cycle#4
                BeforePollingCycleEvent.class,
                AfterPollingCycleEvent.class);
        Mockito.verifyNoMoreInteractions(listenerMock);
    }

    @Test
    public void initialContent() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list("fileA/1", "fileB/1"))
                .thenReturn(list("fileA/1", "fileB/1"));

        final List<FileElement> files = new ArrayList<>();
        Mockito.doAnswer(new Answer<InitialContentEvent>() {

            @Override
            public InitialContentEvent answer(InvocationOnMock invocation) throws Throwable {
                Set<FileElement> s = ((InitialContentEvent) invocation.getArguments()[0]).getFileElements();
                files.addAll(s);
                return null;
            }
        }).when(listenerMock).initialContent(Mockito.any(InitialContentEvent.class));

        // when
        executeNumberOfPollCycles(2);

        // then
        assertThat(files).containsAll(list("fileA/1", "fileB/1"));
        verifyEventsInOrder(
                // poll-cycle#1
                BeforePollingCycleEvent.class,
                InitialContentEvent.class,
                AfterPollingCycleEvent.class,

                // poll-cycle#2
                BeforePollingCycleEvent.class,
                AfterPollingCycleEvent.class);
        Mockito.verifyNoMoreInteractions(listenerMock);
    }

}
