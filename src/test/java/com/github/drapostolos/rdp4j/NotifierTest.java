package com.github.drapostolos.rdp4j;

import java.util.HashSet;
import java.util.Set;

import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;


public class NotifierTest {
	
	@Test
	public void constructNotifierWithNoListener() throws Exception {
		// when
		ListenerNotifier n = new ListenerNotifier(new HashSet<Adp4jListener>());
		
		// then
		Assertions.assertThat(n.listenerToEventsMappings.size()).isEqualTo(0);
	}

	@Test
	public void constructNotifierWithOneListener() throws Exception {
		// given
		Set<Adp4jListener> l = new HashSet<Adp4jListener>();
		l.add(new Adp4jListener(){});
		
		// when
		ListenerNotifier n = new ListenerNotifier(l);
		
		// then
		Assertions.assertThat(n.listenerToEventsMappings.size()).isEqualTo(1);
	}
	
	@Test
	public void AddAndRemoveListeners() throws Exception {
		ListenerNotifier n = new ListenerNotifier(new HashSet<Adp4jListener>());
		Assertions.assertThat(n.listenerToEventsMappings.size()).isEqualTo(0);
		
		Adp4jListener l = new Adp4jListener(){};
		n.addListener(l);
		Assertions.assertThat(n.listenerToEventsMappings.size()).isEqualTo(1);
		n.removeListener(l);
		Assertions.assertThat(n.listenerToEventsMappings.size()).isEqualTo(0);
	}
	
	@Test
	public void addSameListenerTwice() throws Exception {
		// given
		ListenerNotifier n = new ListenerNotifier(new HashSet<Adp4jListener>());
		
		// when
		Adp4jListener l = new Adp4jListener(){};
		n.addListener(l);
		n.addListener(l);
		
		// then 
		Assertions.assertThat(n.listenerToEventsMappings.size()).isEqualTo(1);
	}
	
	@Test
	public void notifyListenersOfFileAddedEvent() throws Exception {
		// given
		ListenerNotifier n = new ListenerNotifier(new HashSet<Adp4jListener>());
		DirectoryListener l1 = Mockito.mock(DirectoryListener.class);
		IoErrorListener l2 = Mockito.mock(IoErrorListener.class);
		DirectoryListener l3 = Mockito.mock(DirectoryListener.class);
		IoErrorListener l4 = Mockito.mock(IoErrorListener.class);
		
		n.addListener(l1);
		n.addListener(l2);
		n.addListener(l3);
		n.addListener(l4);
		
		// when
		FileAddedEvent e = new FileAddedEvent(null, null, null);
		n.notifyListeners(e);
//		n.notifyListeners(e);
		
		// then
		Mockito.verify(l1).fileAdded(e);
		Mockito.verifyNoMoreInteractions(l1);
		Mockito.verify(l3).fileAdded(e);
		Mockito.verifyNoMoreInteractions(l3);
		Mockito.verifyZeroInteractions(l2);
		Mockito.verifyZeroInteractions(l4);
		
	}
	
	
	

}
