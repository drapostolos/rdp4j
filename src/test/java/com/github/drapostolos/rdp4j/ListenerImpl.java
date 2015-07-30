package com.github.drapostolos.rdp4j;

public class ListenerImpl extends AbstractRdp4jListener {

    @Override
    public void afterPollingCycle(AfterPollingCycleEvent event) {
        System.out.println(event);
    }

    @Override
    public void beforePollingCycle(BeforePollingCycleEvent event) {
        System.out.println(event);
    }

    @Override
    public void fileAdded(FileAddedEvent event) {
        System.out.println(event);
    }

    @Override
    public void fileModified(FileModifiedEvent event) {
        System.out.println(event);
    }

    @Override
    public void afterStop(AfterStopEvent event) {
        System.out.println(event);
    }

    @Override
    public void beforeStart(BeforeStartEvent event) {
        System.out.println(event);
    }

    @Override
    public void fileRemoved(FileRemovedEvent event) {
        System.out.println(event);
    }

    @Override
    public void initialContent(InitialContentEvent event) {
        System.out.println(event);
    }

}
