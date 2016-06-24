package com.github.drapostolos.rdp4j;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.never;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

public class AcceptanceTest extends EventVerifier {

    private DirectoryPoller dp;
    private DirectoryPollerBuilder builder;

    @Before
    public void testFixture() throws Exception {
        directoryMock = Mockito.mock(PolledDirectory.class);
        listenerMock = Mockito.mock(AbstractRdp4jListener.class);
        inOrder = Mockito.inOrder(listenerMock);
        builder = DirectoryPoller.newBuilder();
    }

    @After
    public void cleanup() throws Exception {
        if (dp != null && !dp.isTerminated()) {
            dp.stop();
        }
    }

    @Test(timeout = 1000)
    public void canInterruptBeforePollingCycle() throws Exception {

        // given 
        final StringBuffer checker = new StringBuffer();
        final CountDownLatch latch = new CountDownLatch(1);
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list("file1.txt/1"));

        // when
        dp = builder
                .addPolledDirectory(directoryMock)
                .addListener(new AbstractRdp4jListener() {

                    @Override
                    public void beforePollingCycle(BeforePollingCycleEvent event) throws InterruptedException {
                        latch.countDown();
                        SECONDS.sleep(5);
                    }

                    @Override
                    public void afterPollingCycle(AfterPollingCycleEvent event) throws InterruptedException {
                        checker.append("X"); // this should not be called.
                    }

                    @Override
                    public void afterStop(AfterStopEvent event) {
                        checker.append("PASS");
                    }
                })
                .enableFileAddedEventsForInitialContent()
                .setPollingInterval(10, TimeUnit.MILLISECONDS)
                .start();

        // then
        latch.await();
        dp.stopNow();

        assertThat(checker.toString()).isEqualTo("PASS");
    }

    @Test(timeout = 1000)
    public void canInterruptAfterPollingCycle() throws Exception {

        // given 
        final StringBuffer checker = new StringBuffer();
        final CountDownLatch latch = new CountDownLatch(1);
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list("file1.txt/1"));

        // when
        dp = builder
                .addPolledDirectory(directoryMock)
                .addListener(new AbstractRdp4jListener() {

                    @Override
                    public void afterPollingCycle(AfterPollingCycleEvent event) throws InterruptedException {
                        latch.countDown();
                        SECONDS.sleep(5);
                    }

                    @Override
                    public void afterStop(AfterStopEvent event) {
                        checker.append("PASS");
                    }
                })
                .enableFileAddedEventsForInitialContent()
                .setPollingInterval(10, TimeUnit.MILLISECONDS)
                .start();

        // then
        latch.await();
        dp.stopNow();

        assertThat(checker.toString()).isEqualTo("PASS");
    }

    @Test(timeout = 1000)
    public void canInterruptFileAddedEvent() throws Exception {

        // given 
        final StringBuffer checker = new StringBuffer();
        final CountDownLatch latch = new CountDownLatch(1);
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list("file1.txt/1"));

        // when
        dp = builder
                .addPolledDirectory(directoryMock)
                .addListener(new AbstractRdp4jListener() {

                    @Override
                    public void fileAdded(FileAddedEvent event) throws InterruptedException {
                        latch.countDown();
                        SECONDS.sleep(5);
                    }

                    @Override
                    public void afterPollingCycle(AfterPollingCycleEvent event) throws InterruptedException {
                        checker.append("X"); // this should not be called.
                    }

                    @Override
                    public void afterStop(AfterStopEvent event) {
                        checker.append("PASS");
                    }
                })
                .enableFileAddedEventsForInitialContent()
                .setPollingInterval(10, TimeUnit.MILLISECONDS)
                .start();

        // then
        latch.await();
        dp.stopNow();

        assertThat(checker.toString()).isEqualTo("PASS");
    }

    @Test(timeout = 1000)
    public void canInterruptWhenProcessingMultipleListeners() throws Exception {

        // given 
        final CountDownLatch latch = new CountDownLatch(1);
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list("file1.txt/1"));

        // when
        dp = builder
                .addPolledDirectory(directoryMock)
                .addListener(new AbstractRdp4jListener() {

                    @Override
                    public void beforePollingCycle(BeforePollingCycleEvent event) throws InterruptedException {
                        latch.countDown();
                        SECONDS.sleep(10);
                    }
                })
                .addListener(new AbstractRdp4jListener() {

                    @Override
                    public void beforePollingCycle(BeforePollingCycleEvent event) throws InterruptedException {
                        latch.countDown();
                        SECONDS.sleep(10);
                    }
                })
                .enableFileAddedEventsForInitialContent()
                .setPollingInterval(10, TimeUnit.MILLISECONDS)
                .start();

        // then
        latch.await();
        dp.stopNow();
    }

    @Test
    public void canStartAsynchronously() throws Exception {
        // given 
        final CountDownLatch latch = new CountDownLatch(1);

        // when
        DirectoryPollerFuture future = builder
                .addPolledDirectory(directoryMock)
                .addListener(new AbstractRdp4jListener() {

                    @Override
                    public void beforeStart(BeforeStartEvent event) {
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            fail("should not be interrupted here!");
                        }
                    }
                })
                .setPollingInterval(10, TimeUnit.MILLISECONDS)
                .startAsync();

        assertThat(future.isStarted()).isFalse();
        latch.countDown();
        dp = future.get();
        assertThat(dp).isNotNull();
        assertThat(future.isStarted()).isTrue();

    }

    @Test(timeout = 2000)
    public void shouldStopDirectoryPollerWhenRuntimeExceptionIsThrownByImplementation() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list())
                .thenThrow(new RuntimeException());

        // when
        dp = builder
                .addPolledDirectory(new PolledDirectory() {

                    @Override
                    public Set<FileElement> listFiles() throws IOException {
                        throw new RuntimeException();
                    }
                })
                .setPollingInterval(10, TimeUnit.MILLISECONDS)
                .start();

        // then should stop automatically.
        dp.awaitTermination();

    }

    @Test
    public void enableParallelDirectoryPolling() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list());

        // when
        dp = builder
                .addPolledDirectory(directoryMock)
                .enableParallelPollingOfDirectories()
                .setPollingInterval(10, TimeUnit.MILLISECONDS)
                .start();

        // then
        Assertions.assertThat(dp.isParallelDirectoryPollingEnabled()).isTrue();
    }

    @Test
    public void defaultParallelDirectoryPolling() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list());

        // when
        dp = builder
                .addPolledDirectory(directoryMock)
                .start();

        // then
        Assertions.assertThat(dp.isParallelDirectoryPollingEnabled()).isFalse();
    }

    @Test
    public void enableFileAddedEventsForInitialContent() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list());

        // when
        dp = builder
                .addPolledDirectory(directoryMock)
                .enableFileAddedEventsForInitialContent()
                .setPollingInterval(10, TimeUnit.MILLISECONDS)
                .start();

        // then
        Assertions.assertThat(dp.isFileAdedEventForInitialContentEnabled()).isTrue();
    }

    @Test
    public void defaultFileAddedEventsForInitialContent() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list());

        // when
        dp = builder
                .addPolledDirectory(directoryMock)
                .start();

        // then
        Assertions.assertThat(dp.isFileAdedEventForInitialContentEnabled()).isFalse();
    }

    @Test
    public void fileAddedEventEnabledForInitialContent() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list("file1.txt/1"));

        // when
        dp = builder
                .addPolledDirectory(directoryMock)
                .addListener(new PollCycleCounter().stopPollingAfterNumOfCycles(2))
                .addListener(listenerMock)
                .enableFileAddedEventsForInitialContent()
                .setPollingInterval(10, TimeUnit.MILLISECONDS)
                .start();
        dp.awaitTermination();

        // then
        verifyEventsInOrder(
                BeforeStartEvent.class,
                BeforePollingCycleEvent.class,
                FileAddedEvent.class,
                InitialContentEvent.class,
                AfterPollingCycleEvent.class,
                BeforePollingCycleEvent.class,
                AfterPollingCycleEvent.class,
                AfterStopEvent.class);
    }

    @Test
    public void addRemoveDirectories() throws Exception {
        // given 
        PolledDirectory directoryMock2 = Mockito.mock(PolledDirectory.class);

        // when
        PollCycleCounter latch = new PollCycleCounter();
        dp = builder
                .addPolledDirectory(directoryMock)
                .addListener(latch)
                .setPollingInterval(5, TimeUnit.MILLISECONDS)
                .start();
        dp.addPolledDirectory(directoryMock2);

        // then 
        latch.awaitAtLeastNumPollCycles(1);
        Assertions.assertThat(dp.getPolledDirectories()).contains(directoryMock, directoryMock2);

        // when 
        dp.removePolledDirectory(directoryMock);
        dp.removePolledDirectory(directoryMock2);

        // then
        latch.awaitAtLeastNumPollCycles(1);
        Assertions.assertThat(dp.getPolledDirectories()).isEmpty();
    }

    // listenerCanReceiveFileAddedEventWhenListenerAddedAfterStart
    @Test
    public void listenerCanReceiveFileAddedEventWhenListenerAddedAfterStart() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list())
                .thenReturn(list())
                .thenReturn(list("file.txt/12"));

        // when
        PollCycleCounter latch = new PollCycleCounter();
        dp = DirectoryPoller.newBuilder()
                .addListener(latch)
                .addPolledDirectory(directoryMock)
                .setPollingInterval(10, MILLISECONDS)
                .start();
        dp.addListener(listenerMock);
        latch.awaitAtLeastNumPollCycles(5);
        dp.stop();

        // then
        verifyEventsInOrder(
                BeforePollingCycleEvent.class,
                FileAddedEvent.class,
                AfterPollingCycleEvent.class);
    }

    @Test
    public void removeListener() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list())
                .thenReturn(list("file.txt/12"));

        // when
        PollCycleCounter counter = new PollCycleCounter();
        dp = builder
                .addPolledDirectory(directoryMock)
                .addListener(counter)
                .addListener(listenerMock)
                .setPollingInterval(10, TimeUnit.MILLISECONDS)
                .start();
        counter.awaitAtLeastNumPollCycles(1);
        dp.removeListener(listenerMock);
        counter.awaitAtLeastNumPollCycles(1);

        verifyEventsInOrder(
                BeforePollingCycleEvent.class,
                AfterPollingCycleEvent.class);
        Mockito.verify(listenerMock, never()).afterStop(Mockito.any(AfterStopEvent.class));
    }

    @Test
    public void toStringValue() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list());

        // when
        dp = builder
                .addPolledDirectory(directoryMock)
                .start();

        Assertions.assertThat(dp.toString()).matches("DirectoryPoller-\\d+: .*\\[polling every: 1000 milliseconds\\]");

    }

    @Test
    public void OneSuccesfulPoll() throws Exception {
        // given 
        Mockito.when(directoryMock.listFiles())
                .thenReturn(list());

        // when
        PollCycleCounter counter = new PollCycleCounter();
        dp = builder
                .addListener(listenerMock)
                .addListener(counter.stopPollingAfterNumOfCycles(1))
                .addPolledDirectory(directoryMock)
                .setPollingInterval(20, MILLISECONDS)
                .start();
        dp.awaitTermination();

        // then
        Assertions.assertThat(dp.getThreadName()).matches("DirectoryPoller-\\d+");
        verifyEventsInOrder(
                BeforeStartEvent.class,
                BeforePollingCycleEvent.class,
                InitialContentEvent.class,
                AfterPollingCycleEvent.class,
                AfterStopEvent.class);
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
        dp = builder
                .addListener(new PollCycleCounter().stopPollingAfterNumOfCycles(1))
                .addListener(listenerMock)
                .addPolledDirectory(directoryMock)
                .setDefaultFileFilter(new RegexFileFilter(".*\\.txt"))
                .setThreadName("NAME")
                .setPollingInterval(20, TimeUnit.MILLISECONDS)
                .start();
        dp.awaitTermination();

        // then
        Assertions.assertThat(files).containsExactly(array("a.txt/12"));
        Assertions.assertThat(dp.getThreadName()).isEqualTo("NAME");
        Assertions.assertThat(dp.getPollingIntervalInMillis()).isEqualTo(20);
        verifyEventsInOrder(
                BeforeStartEvent.class,
                BeforePollingCycleEvent.class,
                InitialContentEvent.class,
                AfterPollingCycleEvent.class,
                AfterStopEvent.class);
        Mockito.verifyNoMoreInteractions(listenerMock);
    }
}
