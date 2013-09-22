package com.github.drapostolos.rdp4j;

/**
 * A listener of the {@link BeforePollingCycleEvent}/{@link AfterPollingCycleEvent} 
 * events of the {@link DirectoryPoller}.
 *
 */
public interface PollCycleListener extends Adp4jListener{

	/**
	 * Invoked before each poll-cycle start.
	 * 
	 * @param event provided by the {@link DirectoryPoller}.
	 */
	void beforePollingCycle(BeforePollingCycleEvent event);

	/**
	 * Invoked after each poll-cycle end.
	 * 
	 * @param event provided by the {@link DirectoryPoller}.
	 */
	void afterPollingCycle(AfterPollingCycleEvent event);

}
