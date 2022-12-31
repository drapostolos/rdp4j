package com.github.drapostolos.rdp4j;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockito.Mockito;

import com.github.drapostolos.rdp4j.spi.PolledDirectory;

public class DirectoryPollerBuilderTest {

    DirectoryPollerBuilder builder = DirectoryPoller.newBuilder();

    @Test(expected = NullPointerException.class)
    public void nullDirectory() throws Exception {
        builder.addPolledDirectory(null);
    }

    @Test(expected = NullPointerException.class)
    public void nullPreviousState() throws Exception {
    	Set<CachedFileElement> previousState = null;
        builder.addPolledDirectory(Mockito.mock(PolledDirectory.class), previousState);
    }

    @Test(expected = NullPointerException.class)
    public void nullPreviousStateVarArg() throws Exception {
    	CachedFileElement[] previousState = null;
        builder.addPolledDirectory(Mockito.mock(PolledDirectory.class), previousState);
    }

    @Test(expected = NullPointerException.class)
    public void nullFileFilter() throws Exception {
        builder.setDefaultFileFilter(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativePollingInterval() throws Exception {
        builder.setPollingInterval(-1, TimeUnit.SECONDS);
    }

    @Test(expected = NullPointerException.class)
    public void nullThreadName() throws Exception {
        builder.setThreadName(null);
    }

    @Test(expected = NullPointerException.class)
    public void nullListener() throws Exception {
        builder.addListener(null);
    }

}
