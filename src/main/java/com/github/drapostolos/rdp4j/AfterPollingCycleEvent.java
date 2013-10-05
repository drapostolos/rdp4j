package com.github.drapostolos.rdp4j;

/**
 * An event that is triggered after each poll-cycle of the {@link DirectoryPoller}.
 * 
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User Guide</a>.
 */
public final class AfterPollingCycleEvent extends Event{

	AfterPollingCycleEvent(DirectoryPoller dp) {
		super(dp);
	}

}
