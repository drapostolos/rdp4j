package com.github.drapostolos.rdp4j;

import java.util.HashMap;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ScheduledRunnable.class)
public class ParallelPollerTest extends EventVerifier {

    @Test
    public void singleThreadExecutor() throws Exception {
        // given
        PowerMockito.mockStatic(Executors.class);
        DirectoryPoller dp = Mockito.mock(DirectoryPoller.class);
        dp.directories = new HashMap<>();
        dp.parallelDirectoryPollingEnabled = false;

        // when
        pollerTask = new ScheduledRunnable(dp);

        // then
        PowerMockito.verifyStatic(Executors.class);
        Executors.newSingleThreadExecutor();
    }

    @Test
    public void cachedThreadPool() throws Exception {
        // given
        PowerMockito.mockStatic(Executors.class);
        DirectoryPoller dp = Mockito.mock(DirectoryPoller.class);
        dp.directories = new HashMap<>();
        dp.parallelDirectoryPollingEnabled = true;

        // when
        pollerTask = new ScheduledRunnable(dp);

        // then
        PowerMockito.verifyStatic(Executors.class);
        Executors.newCachedThreadPool();
    }
}
