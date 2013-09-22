package com.github.drapostolos.rdp4j;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.mockito.InOrder;
import org.mockito.Mockito;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

class EventVerifier {

	protected PollerTask pollerTask;
	protected AbstractAdp4jListener listenerMock;
	protected InOrder inOrder;
	protected Set<PolledDirectory> directories = new LinkedHashSet<PolledDirectory>();
	protected PolledDirectory directoryMock;
	protected DirectoryPoller directoryPollerMock;

	protected void executeNumberOfPollCycles(int numOfPollCycles) {
		for(int i = 0; i < numOfPollCycles; i++){
			pollerTask.run();
		}
	}

	/*
	 * Dispatch event to the "verifyInOrder_" methods below.
	 */
	protected void verifyEventsInOrder(Class<?>... events) throws Exception {
		for(Class<?> event : events){
			Method m = getClass().getMethod("verifyInOrder_" + event.getSimpleName());
			m.invoke(this);
		}
	}

	public void verifyInOrder_InitialContentEvent() {
		inOrder.verify(listenerMock).initialContent(Mockito.any(InitialContentEvent.class));
	}
	public void verifyInOrder_BeforePollingCycleEvent() {
		inOrder.verify(listenerMock).beforePollingCycle(Mockito.any(BeforePollingCycleEvent.class));
	}
	public void verifyInOrder_AfterPollingCycleEvent() {
		inOrder.verify(listenerMock).afterPollingCycle(Mockito.any(AfterPollingCycleEvent.class));
	}

	public void verifyInOrder_FileAddedEvent() {
		inOrder.verify(listenerMock).fileAdded(Mockito.any(FileAddedEvent.class));
	}
	public void verifyInOrder_FileRemovedEvent() {
		inOrder.verify(listenerMock).fileRemoved(Mockito.any(FileRemovedEvent.class));
	}
	public void verifyInOrder_FileModifiedEvent() {
		inOrder.verify(listenerMock).fileModified(Mockito.any(FileModifiedEvent.class));
	}

	public void verifyInOrder_IoErrorCeasedEvent() {
		inOrder.verify(listenerMock).ioErrorCeased(Mockito.any(IoErrorCeasedEvent.class));
	}
	public void verifyInOrder_IoErrorRaisedEvent() {
		inOrder.verify(listenerMock).ioErrorRaised(Mockito.any(IoErrorRaisedEvent.class));
	}

	public void verifyInOrder_BeforeStartEvent() {
		inOrder.verify(listenerMock).beforeStart(Mockito.any(BeforeStartEvent.class));
	}
	public void verifyInOrder_AfterStopEvent() {
		inOrder.verify(listenerMock).afterStop(Mockito.any(AfterStopEvent.class));
	}

	/*
	 * input argument is in the form: "file-name/lastModified"
	 * Example "my.txt/1233"
	 */
	public Set<FileElement> list(String... files) throws Exception {
		Set<FileElement> result = new LinkedHashSet<FileElement>();
		for(String nameAndTime : files){
			String[] t = nameAndTime.split("/");
			String fileName = t[0];
			long lastModified = Long.parseLong(t[1]);
			FileElement file = new StubbedFileObject(fileName, lastModified);
			result.add(file);
		}
		return result;
	}

}
