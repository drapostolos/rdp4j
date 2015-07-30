package com.github.drapostolos.rdp4j;

/**
 * A listener of {@link IoErrorRaisedEvent}/{@link IoErrorCeasedEvent} events
 * of the {@link DirectoryPoller}.
 * 
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public interface IoErrorListener extends Rdp4jListener {

    /**
     * Invoked when an I/O error occurs in the {@link DirectoryPoller}.
     * <p>
     * NOTE! Any consecutive poll-cycles with I/O errors will not fire
     * any {@link IoErrorRaisedEvent}, not until a {@link IoErrorCeasedEvent}
     * event has been fired first.
     * 
     * @param event provided by the {@link DirectoryPoller}.
     * @throws InterruptedException when interrupted.
     */
    void ioErrorRaised(IoErrorRaisedEvent event) throws InterruptedException;

    /**
     * Invoked when the {@link DirectoryPoller} has recovered from an
     * I/O error.
     * 
     * @param event provided by the {@link DirectoryPoller}.
     * @throws InterruptedException when interrupted.
     */
    void ioErrorCeased(IoErrorCeasedEvent event) throws InterruptedException;
}
