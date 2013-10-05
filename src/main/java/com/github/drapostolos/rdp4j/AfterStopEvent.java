package com.github.drapostolos.rdp4j;

/**
 * An event that represents the stopping of the {@link DirectoryPoller}, as 
 * triggered by this method: {@link DirectoryPoller#stop()}. 
 * 
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User Guide</a>.
 */
public final class AfterStopEvent extends Event {

	AfterStopEvent(DirectoryPoller directoryPoller) {
		super(directoryPoller);
	}
	
}
