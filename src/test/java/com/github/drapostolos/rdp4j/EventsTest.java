package com.github.drapostolos.rdp4j;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

public class EventsTest {

    @Test
    public void ioExceptionEvent() throws Exception {
        // given
        DirectoryPoller dp = Mockito.mock(DirectoryPoller.class);
        PolledDirectory directory = Mockito.mock(PolledDirectory.class);
        IOException e = Mockito.mock(IOException.class);

        // when
        IoErrorRaisedEvent event = new IoErrorRaisedEvent(dp, directory, e);

        // then
        assertThat(event.getIoException()).isEqualTo(e);
        assertThat(event.getPolledDirectory()).isEqualTo(directory);
        assertThat(event.getDirectoryPoller()).isEqualTo(dp);

    }

    @Test
    public void fileAddedEvent() throws Exception {
        // given
        DirectoryPoller dp = Mockito.mock(DirectoryPoller.class);
        PolledDirectory directory = Mockito.mock(PolledDirectory.class);
        FileElement file = Mockito.mock(FileElement.class);

        // when
        FileAddedEvent event = new FileAddedEvent(dp, directory, file);

        // then
        assertThat(event.getFileElement()).isEqualTo(file);
        assertThat(event.getPolledDirectory()).isEqualTo(directory);
        assertThat(event.getDirectoryPoller()).isEqualTo(dp);

    }

}
