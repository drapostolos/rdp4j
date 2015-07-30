package com.github.drapostolos.rdp4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class PollCycleCounter extends AbstractRdp4jListener {

    private int cycleCounter = 0;
    private int numOfcyclesBeforeStop = Integer.MAX_VALUE;
    private boolean debugEnabled = false;
    private ConcurrentLinkedQueue<CountDownLatch> queue = new ConcurrentLinkedQueue<CountDownLatch>();
    private Runnable action;
    private int countUntilAction = -1;

    public PollCycleCounter stopPollingAfterNumOfCycles(int i) {
        numOfcyclesBeforeStop = i;
        return this;
    }

    public PollCycleCounter enableDebug() {
        debugEnabled = true;
        return this;
    }

    public PollCycleCounter onBeforePollingCycleDo(int cycleCount, Runnable runnable) {
        action = runnable;
        countUntilAction = cycleCount;
        return this;
    }

    public void awaitAtLeastNumPollCycles(int num) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(num + 1);
        queue.add(latch);
        latch.await();
    }

    @Override
    public void beforePollingCycle(final BeforePollingCycleEvent event) {
        logEventIfDebugEnabled(event);
        if (cycleCounter >= numOfcyclesBeforeStop - 1) {
            event.getDirectoryPoller().stopAsync();
        }
        for (CountDownLatch latch : queue) {
            latch.countDown();
        }
        if (cycleCounter == countUntilAction) {
            action.run();
        }
        cycleCounter++;
    }

    @Override
    public void initialContent(InitialContentEvent event) {
        logEventIfDebugEnabled(event);
    }

    @Override
    public void afterPollingCycle(AfterPollingCycleEvent event) {
        logEventIfDebugEnabled(event);
    }

    @Override
    public void afterStop(AfterStopEvent event) {
        logEventIfDebugEnabled(event);
    }

    @Override
    public void beforeStart(BeforeStartEvent event) {
        logEventIfDebugEnabled(event);
    }

    @Override
    public void fileAdded(FileAddedEvent event) {
        logEventIfDebugEnabled(event);
    }

    @Override
    public void fileModified(FileModifiedEvent event) {
        logEventIfDebugEnabled(event);
    }

    @Override
    public void fileRemoved(FileRemovedEvent event) {
        logEventIfDebugEnabled(event);
    }

    @Override
    public void ioErrorCeased(IoErrorCeasedEvent event) {
        logEventIfDebugEnabled(event);
    }

    @Override
    public void ioErrorRaised(IoErrorRaisedEvent event) {
        logEventIfDebugEnabled(event);
    }

    private void logEventIfDebugEnabled(Event event) {
        if (debugEnabled) {
            System.out.println("# " + event.getClass().getSimpleName() + "; ThreadId: "
                    + Thread.currentThread().getId());
        }
    }

}
