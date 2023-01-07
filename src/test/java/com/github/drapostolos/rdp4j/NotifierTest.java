package com.github.drapostolos.rdp4j;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

public class NotifierTest {
	
	Logger notifierLogger = Mockito.mock(Logger.class);

	@Test
	public void shouldLogWithErrorLevelWhenListenerThrows() throws Exception {
		// Given
		InitialContentListener listener = e -> {
			throw new RuntimeException("test");
		};
		Set<Rdp4jListener> listeners = new HashSet<Rdp4jListener>();
		listeners.add(listener);
		ListenerNotifier notifier = new ListenerNotifier(notifierLogger, listeners);
		
		// When
		notifier.initialContent(null);
		
		// Then
		Mockito.verify(notifierLogger)
		.error(Mockito.contains("Exception thrown by client implementation"), (Throwable) Mockito.any());
	}
	
    @Test
    public void canCheckIfListenerIsInstanceOfSpecificListenerInterface() throws Exception {
        Set<Rdp4jListener> listeners = new HashSet<Rdp4jListener>();
        DirectoryPollerListener listener = new DirectoryPollerListener() {

            @Override
            public void beforeStart(BeforeStartEvent event) {}

            @Override
            public void afterStop(AfterStopEvent event) {}
        };
        listeners.add(listener);

        // when
        ListenerNotifier notifier = new ListenerNotifier(notifierLogger, listeners);

        // then
        assertThat(notifier.isInstanceOf(listener, DirectoryPollerListener.class)).isTrue();
        assertThat(notifier.isInstanceOf(listener, DirectoryListener.class)).isFalse();

    }

    @Test
    public void constructNotifierWithNoListener() throws Exception {
        // when
        ListenerNotifier n = new ListenerNotifier(notifierLogger, new HashSet<Rdp4jListener>());

        // then
        assertThat(n.listeners.size()).isEqualTo(0);
    }

    @Test
    public void constructNotifierWithOneListener() throws Exception {
        // given
        Set<Rdp4jListener> l = new HashSet<Rdp4jListener>();
        l.add(new Rdp4jListener() {});

        // when
        ListenerNotifier n = new ListenerNotifier(notifierLogger, l);

        // then
        assertThat(n.listeners.size()).isEqualTo(1);
    }

    @Test
    public void AddAndRemoveListeners() throws Exception {
        ListenerNotifier n = new ListenerNotifier(notifierLogger, new HashSet<Rdp4jListener>());
        assertThat(n.listeners.size()).isEqualTo(0);

        Rdp4jListener l = new Rdp4jListener() {};
        n.addListener(l);
        assertThat(n.listeners.size()).isEqualTo(1);
        n.removeListener(l);
        assertThat(n.listeners.size()).isEqualTo(0);
    }

    @Test
    public void addSameListenerTwice() throws Exception {
        // given
        ListenerNotifier n = new ListenerNotifier(notifierLogger, new HashSet<Rdp4jListener>());

        // when
        Rdp4jListener l = new Rdp4jListener() {};
        n.addListener(l);
        n.addListener(l);

        // then 
        assertThat(n.listeners.size()).isEqualTo(1);
    }

    @Test
    public void notifyListenersOfFileAddedEvent() throws Exception {
        // given
        ListenerNotifier n = new ListenerNotifier(notifierLogger, new HashSet<Rdp4jListener>());
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
        n.fileAdded(e);
        //		n.notifyListeners(e);

        // then
        Mockito.verify(l1).fileAdded(e);
        Mockito.verifyNoMoreInteractions(l1);
        Mockito.verify(l3).fileAdded(e);
        Mockito.verifyNoMoreInteractions(l3);
        Mockito.verifyNoInteractions(l2);
        Mockito.verifyNoInteractions(l4);

    }

}
