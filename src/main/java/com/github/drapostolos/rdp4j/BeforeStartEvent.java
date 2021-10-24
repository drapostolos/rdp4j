package com.github.drapostolos.rdp4j;

import java.util.Set;

import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * An event that represents the start the {@link DirectoryPoller}, as
 * triggered by this method: {@link DirectoryPollerBuilder#start()}.
 * 
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public final class BeforeStartEvent implements Event {
	private DirectoryPollerBuilder builder;

	BeforeStartEvent(DirectoryPollerBuilder builder) {
		this.builder = builder;
	}

	/**
	 * @see DirectoryPollerBuilder#addPolledDirectory(PolledDirectory, CachedFileElement...)
	 * 
	 * @param directory
	 * @param previousState
	 */
	public void addPolledDirectory(PolledDirectory directory, CachedFileElement... previousState) {
		builder.addPolledDirectory(directory, previousState);
	}

	/**
	 * @see DirectoryPollerBuilder#addPolledDirectory(PolledDirectory, Set)
	 * 
	 * @param directory
	 * @param previousState
	 */
	public void addPolledDirectory(PolledDirectory directory, Set<CachedFileElement> previousState) {
		builder.addPolledDirectory(directory, previousState);
	}

}
