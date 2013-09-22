package com.github.drapostolos.rdp4j;

/**
 * An event that represents the stopping of the {@link DirectoryPoller}, as 
 * triggered by this method: {@link DirectoryPoller#stop()}. 
 */
public final class AfterStopEvent extends Event {

	AfterStopEvent(DirectoryPoller directoryPoller) {
		super(directoryPoller);
	}
	
}
