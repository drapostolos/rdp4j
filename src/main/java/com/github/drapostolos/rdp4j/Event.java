package com.github.drapostolos.rdp4j;


abstract class Event{
	final DirectoryPoller dp;

	Event(DirectoryPoller directoryPoller) {
		dp = directoryPoller;
	}
	
	/**
	 * Returns the {@link DirectoryPoller} instance which fired
	 * this event.
	 */
	public DirectoryPoller getDirectoryPoller(){
		return dp;
	}
	
}
