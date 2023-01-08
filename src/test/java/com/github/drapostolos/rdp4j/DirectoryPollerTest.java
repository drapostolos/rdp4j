package com.github.drapostolos.rdp4j;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.github.drapostolos.rdp4j.spi.PolledDirectory;

public class DirectoryPollerTest {

    private DirectoryPoller dp;
    private DirectoryPollerBuilder builder;
    private Path persistedFile = Paths.get("file.dat");

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void testFixture() throws Exception {
        builder = DirectoryPoller.newBuilder();
    }

    @After
    public void cleanup() throws Exception {
        if (dp != null) {
            dp.stop();
        }
        Files.deleteIfExists(persistedFile);
    }
    
    @Test
	public void shouldThrowIfDefaultPersisterFileIsDirectory() throws Exception {
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("Persisted file cannot be a directory");
        expectedEx.expectMessage(Paths.get("").toAbsolutePath().toString());

        builder.enableDefaultStatePersisting(Paths.get(""), dir -> "", str -> null);
	}

    @Test
	public void shouldNotThrowIfDefaultPersisterFileiExists() throws Exception {
    	Files.createFile(persistedFile);

        builder.enableDefaultStatePersisting(persistedFile, dir -> "", str -> null);
	}

    @Test
    public void shouldThrowExceptionWhenNoPolledDirectorySetAtStart() {
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage(String.format("Unable to start the '%s'", DirectoryPoller.class.getSimpleName()));
        expectedEx.expectMessage(
                String.format("%s.addPolledDirectory(PolledDirectory)", DirectoryPollerBuilder.class.getSimpleName()));
        DirectoryPoller.newBuilder().start();
    }

    @Test
    public void shouldHaveSameNumberOfActiveThreadsBeforeStartAndAfterStop() throws Exception {
        // given
        PolledDirectory directoryMock = Mockito.mock(PolledDirectory.class);
        Set<Thread> threadsBefore = new HashSet<Thread>(Thread.getAllStackTraces().keySet());

        // when
        dp = builder
                .addPolledDirectory(directoryMock)
                .setPollingInterval(1, TimeUnit.MILLISECONDS)
                .addListener(new PollCycleCounter().stopPollingAfterNumOfCycles(3))
                .enableParallelPollingOfDirectories()
                .start();
        dp.awaitTermination();

        //        TimeUnit.MILLISECONDS.sleep(50);
        // then
        assertThat(Thread.getAllStackTraces().keySet()).containsAll(threadsBefore);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenAddingDirectoryThatIsNull() {
        // given
        PolledDirectory directoryMock = Mockito.mock(PolledDirectory.class);
        dp = builder
                .addPolledDirectory(directoryMock)
                .start();

        // when
        dp.addPolledDirectory(null);
    }

    @Test(expected = NullPointerException.class)
    public void removeNullDirectory() {
        // given
        PolledDirectory directoryMock = Mockito.mock(PolledDirectory.class);
        dp = builder
                .addPolledDirectory(directoryMock)
                .start();

        // when
        dp.removePolledDirectory(null);
    }

    @Test(expected = NullPointerException.class)
    public void addNullListener() {
        // given
        PolledDirectory directoryMock = Mockito.mock(PolledDirectory.class);
        dp = builder
                .addPolledDirectory(directoryMock)
                .start();

        // when
        dp.addListener(null);
    }

    @Test(expected = NullPointerException.class)
    public void removeNullListener() {
        // given
        PolledDirectory directoryMock = Mockito.mock(PolledDirectory.class);
        dp = builder
                .addPolledDirectory(directoryMock)
                .start();

        // when
        dp.removeListener(null);
    }

}
