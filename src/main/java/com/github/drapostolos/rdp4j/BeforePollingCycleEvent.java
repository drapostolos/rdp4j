package com.github.drapostolos.rdp4j;

/**
 * An event that is triggered before each poll-cycle of the {@link DirectoryPoller}.
 * 
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User-Guide</a>
 */
public final class BeforePollingCycleEvent extends EventExposingDirectoryPoller {

	BeforePollingCycleEvent(DirectoryPoller dp) {
		super(dp);
	}

}
