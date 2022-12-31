package com.github.drapostolos.rdp4j;

import static org.mockito.Mockito.times;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mockito.InOrder;
import org.mockito.Mockito;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

class EventVerifier {

    protected ScheduledRunnable pollerTask;
    protected AbstractRdp4jListener listenerMock;
    protected InOrder inOrder;
    Map<PolledDirectory, Set<CachedFileElement>> directories = new HashMap<>();
    protected PolledDirectory directoryMock;
    protected DirectoryPoller directoryPollerMock;

    protected void executeNumberOfPollCycles(int numOfPollCycles) {
        for (int i = 0; i < numOfPollCycles; i++) {
            pollerTask.run();
        }
    }
    
    private static Map.Entry<Class<?>, Integer> entry(Class<?> cls, Integer counter){
    	return new AbstractMap.SimpleEntry<>(cls, counter);
    }

    /*
     * Dispatch event to the "verifyInOrder_" methods below.
     */
    protected void verifyEventsInOrder(Class<?>... events) throws Exception {
    	LinkedList<Entry<Class<?>, Integer>> list = new LinkedList<>();
    	
    	for (Class<?> event : events) {
			if(list.isEmpty()) {
				list.addLast(entry(event, 1));
				continue;
			}
			
			Entry<Class<?>, Integer> last = list.getLast();
			if(!last.getKey().equals(event)) {
				list.addLast(entry(event, 1));
				continue;
			}
			last.setValue(last.getValue() + 1);
		}
        for (Entry<Class<?>, Integer> entry : list) {
        	Class<?> event = entry.getKey();
        	int count = entry.getValue();
            if (event.equals(InitialContentEvent.class)) {
                inOrder.verify(listenerMock, times(count)).initialContent(Mockito.any(InitialContentEvent.class));
            } else if (event.equals(BeforePollingCycleEvent.class)) {
                inOrder.verify(listenerMock, times(count)).beforePollingCycle(Mockito.any(BeforePollingCycleEvent.class));
            } else if (event.equals(AfterPollingCycleEvent.class)) {
                inOrder.verify(listenerMock, times(count)).afterPollingCycle(Mockito.any(AfterPollingCycleEvent.class));
            } else if (event.equals(FileAddedEvent.class)) {
                inOrder.verify(listenerMock, times(count)).fileAdded(Mockito.any(FileAddedEvent.class));
            } else if (event.equals(FileRemovedEvent.class)) {
                inOrder.verify(listenerMock, times(count)).fileRemoved(Mockito.any(FileRemovedEvent.class));
            } else if (event.equals(FileModifiedEvent.class)) {
                inOrder.verify(listenerMock, times(count)).fileModified(Mockito.any(FileModifiedEvent.class));
            } else if (event.equals(IoErrorCeasedEvent.class)) {
                inOrder.verify(listenerMock, times(count)).ioErrorCeased(Mockito.any(IoErrorCeasedEvent.class));
            } else if (event.equals(IoErrorRaisedEvent.class)) {
                inOrder.verify(listenerMock, times(count)).ioErrorRaised(Mockito.any(IoErrorRaisedEvent.class));
            } else if (event.equals(BeforeStartEvent.class)) {
                inOrder.verify(listenerMock, times(count)).beforeStart(Mockito.any(BeforeStartEvent.class));
            } else if (event.equals(AfterStopEvent.class)) {
                inOrder.verify(listenerMock, times(count)).afterStop(Mockito.any(AfterStopEvent.class));
            } else {
                throw new RuntimeException("Missing event in When verifying order: " + event);
            }
        }
    }

    /*
     * input argument is in the form: "file-name/lastModified"
     * Example "my.txt/1233"
     */
    public Set<FileElement> list(String... files) throws Exception {
        Set<FileElement> result = new LinkedHashSet<FileElement>();
        for (String nameAndTime : files) {
            String[] t = nameAndTime.split("/");
            String fileName = t[0];
            long lastModified = Long.parseLong(t[1]);
            FileElement file = new StubbedFileElement(fileName, lastModified);
            result.add(file);
        }
        return result;
    }

    public FileElement[] array(String... files) throws Exception {
        return list(files).toArray(new StubbedFileElement[0]);
    }

}
