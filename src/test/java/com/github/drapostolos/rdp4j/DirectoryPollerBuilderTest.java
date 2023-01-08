package com.github.drapostolos.rdp4j;

import java.nio.file.Paths;
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
    
    @Test(expected = NullPointerException.class)
    public void nullDefaultStatePersisterFile() throws Exception {
		builder.enableDefaultStatePersisting(null, dir -> "", str -> null);
	}

    @Test(expected = NullPointerException.class)
    public void nullDefaultStatePersisterFunction1() throws Exception {
		builder.enableDefaultStatePersisting(Paths.get("file.dat"), null, str -> null);
	}

    @Test(expected = NullPointerException.class)
    public void nullDefaultStatePersisterFunction2() throws Exception {
		builder.enableDefaultStatePersisting(Paths.get("file.dat"), dir -> "", null);
	}

    @Test(expected = NullPointerException.class)
    public void nullStatePersister() throws Exception {
		builder.enableStatePersisting(null);
	}

}
