package com.github.drapostolos.rdp4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListenerImpl extends AbstractRdp4jListener {

    private static final Logger LOG = LoggerFactory.getLogger(ListenerImpl.class);

    @Override
    public void afterPollingCycle(AfterPollingCycleEvent event) {
        LOG.info(event.toString());
    }

    @Override
    public void beforePollingCycle(BeforePollingCycleEvent event) {
        LOG.info(event.toString());
    }

    @Override
    public void fileAdded(FileAddedEvent event) {
        LOG.info(event.toString());
    }

    @Override
    public void fileModified(FileModifiedEvent event) {
        LOG.info(event.toString());
    }

    @Override
    public void afterStop(AfterStopEvent event) {
        LOG.info(event.toString());
    }

    @Override
    public void beforeStart(BeforeStartEvent event) {
        LOG.info(event.toString());
    }

    @Override
    public void fileRemoved(FileRemovedEvent event) {
        LOG.info(event.toString());
    }

    @Override
    public void initialContent(InitialContentEvent event) {
        LOG.info(event.toString());
    }

}
