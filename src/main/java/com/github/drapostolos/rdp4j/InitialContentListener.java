package com.github.drapostolos.rdp4j;

/**
 * A listener of the {@link InitialContentEvent} event of the {@link DirectoryPoller}.
 *
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public interface InitialContentListener extends Rdp4jListener{
	/**
	 * Invoked once during the first poll-cycle of the {@link DirectoryPoller}.
	 * 
	 * @param event provided by the {@link DirectoryPoller}.
	 */
	void initialContent(InitialContentEvent event);

}
