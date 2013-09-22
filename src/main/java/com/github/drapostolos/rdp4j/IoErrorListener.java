package com.github.drapostolos.rdp4j;

/**
 * A listener of {@link IoErrorRaisedEvent}/{@link IoErrorCeasedEvent} events
 * of the {@link DirectoryPoller}.
 */
public interface IoErrorListener extends Adp4jListener{
	
	/**
	 * Invoked when an I/O error occurs in the {@link DirectoryPoller}.
	 * <p>
	 * NOTE! Any consecutive poll-cycles with I/O errors will not fire
	 * any {@link IoErrorRaisedEvent}, not until a {@link IoErrorCeasedEvent}
	 * event has been fired first.
	 * 
	 * @param event provided by the {@link DirectoryPoller}.
	 */
	void ioErrorRaised(IoErrorRaisedEvent event);
	
	/**
	 * Invoked when the {@link DirectoryPoller} has recovered from an 
	 * I/O error. 
	 * 
	 * @param event provided by the {@link DirectoryPoller}.
	 */
	void ioErrorCeased(IoErrorCeasedEvent event);
}
