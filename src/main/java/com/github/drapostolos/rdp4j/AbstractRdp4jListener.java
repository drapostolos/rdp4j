package com.github.drapostolos.rdp4j;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * This is a Skeletal implementation of all {@link Rdp4jListener} sub-interfaces.
 * Contains default implementations of all methods.
 * <p>
 * It is up to the client to implement any desired methods.
 * 
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public abstract class AbstractRdp4jListener implements DirectoryListener, IoErrorListener, DirectoryPollerListener,
        PollCycleListener, InitialContentListener {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRdp4jListener.class);

    /**
     * Dummy implementation doing nothing.
     */
    @Override
    public void beforeStart(BeforeStartEvent event) {

    }

    /**
     * Dummy implementation doing nothing.
     */
    @Override
    public void afterStop(AfterStopEvent event) {

    }

    /**
     * Dummy implementation doing nothing.
     * 
     * @throws InterruptedException when interrupted.
     */
    @Override
    public void beforePollingCycle(BeforePollingCycleEvent event) throws InterruptedException {

    }

    /**
     * Dummy implementation doing nothing.
     * 
     * @throws InterruptedException when interrupted.
     */
    @Override
    public void afterPollingCycle(AfterPollingCycleEvent event) throws InterruptedException {

    }

    /**
     * Logs any occurred I/O errors
     * 
     * @throws InterruptedException when interrupted.
     */
    @Override
    public void ioErrorRaised(IoErrorRaisedEvent event) throws InterruptedException {
        String message = "I/O error raised when polling directory '%s'!";
        PolledDirectory dir = event.getPolledDirectory();
        IOException e = event.getIoException();
        LOG.error(String.format(message, dir), e);
    }

    /**
     * Logs when an I/O error has ceased.
     * 
     * @throws InterruptedException when interrupted.
     */
    @Override
    public void ioErrorCeased(IoErrorCeasedEvent event) throws InterruptedException {
        String message = "I/O error ceased when polling directory '%s'!";
        PolledDirectory dir = event.getPolledDirectory();
        LOG.info(String.format(message, dir));
    }

    /**
     * Dummy implementation doing nothing.
     * 
     * @throws InterruptedException when interrupted.
     */
    @Override
    public void initialContent(InitialContentEvent event) throws InterruptedException {

    }

    /**
     * Dummy implementation doing nothing.
     * 
     * @throws InterruptedException when interrupted.
     */
    @Override
    public void fileAdded(FileAddedEvent event) throws InterruptedException {

    }

    /**
     * Dummy implementation doing nothing.
     * 
     * @throws InterruptedException when interrupted.
     */
    @Override
    public void fileRemoved(FileRemovedEvent event) throws InterruptedException {

    }

    /**
     * Dummy implementation doing nothing.
     * 
     * @throws InterruptedException when interrupted.
     */
    @Override
    public void fileModified(FileModifiedEvent event) throws InterruptedException {

    }
}
