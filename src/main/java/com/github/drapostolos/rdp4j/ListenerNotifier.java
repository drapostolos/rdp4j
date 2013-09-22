package com.github.drapostolos.rdp4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ListenerNotifier {
	private static Logger logger = LoggerFactory.getLogger(ListenerNotifier.class);
	final Map<Rdp4jListener, Set<Class<? extends Event>>> listenerToEventsMappings;
	final Map<Class<? extends Event>, Method> eventToMethodMappings;
	
	ListenerNotifier(Set<Rdp4jListener> listeners) {
		listenerToEventsMappings = new HashMap<Rdp4jListener, Set<Class<? extends Event>>>();
		eventToMethodMappings = new HashMap<Class<? extends Event>, Method>();
		for(Rdp4jListener listener : listeners){
			addListener(listener);
		}
	}

	void addListener(Rdp4jListener listener) {
		if(notAdded(listener)){
			listenerToEventsMappings.put(listener, new HashSet<Class<? extends Event>>());
			extractEventsSupportedByListener(listener);
		}
	}
	
	
	private boolean notAdded(Rdp4jListener listener) {
		return !listenerToEventsMappings.containsKey(listener);
	}

	private void extractEventsSupportedByListener(Rdp4jListener listener) {
		for(Class<?> interfaceType : getAllInterfaceTypesRepresentedBy(listener)){
			if(isListenerType(interfaceType)){
				extractEventsFromInterfaceAndAddToMappings(interfaceType, listener);
			}
		}
	}
	private boolean isListenerType(Class<?> cls) {
		return Rdp4jListener.class.isAssignableFrom(cls);
	}
	private List<Class<?>> getAllInterfaceTypesRepresentedBy(Object o){
		List<Class<?>> allInterfaces = new ArrayList<Class<?>>();
		getAllInterfaces(o.getClass(), allInterfaces);
		return allInterfaces;
	}
	private void getAllInterfaces(Class<?> cls, List<Class<?>> interfaces){
		if(cls == null){
			return;
		}
		interfaces.addAll(Arrays.asList(cls.getInterfaces()));
		getAllInterfaces(cls.getSuperclass(), interfaces);
	}
	private void extractEventsFromInterfaceAndAddToMappings(Class<?> interfaceType, Rdp4jListener listener) {
		for(Method method : interfaceType.getMethods()){
			Class<?>[] paramTypes = method.getParameterTypes();
			if(isEventMethod(paramTypes)){
				/*
				 * This cast is correct since it is assured paramTypes[0]
				 * is assignable to Event.
				 */
				@SuppressWarnings("unchecked")
				Class<? extends Event> eventType = (Class<? extends Event>) paramTypes[0];
				eventToMethodMappings.put(eventType, method);
				listenerToEventsMappings.get(listener).add(eventType);
			}
		}
	}

	private boolean isEventMethod(Class<?>[] paramTypes) {
		return paramTypes.length == 1 && Event.class.isAssignableFrom(paramTypes[0]);
	}

	void removeListener(Rdp4jListener listener) {
		listenerToEventsMappings.remove(listener);
	}

	void notifyListeners(Event event) {
		Class<?> eventType = event.getClass();
		for(Rdp4jListener listener : listenerToEventsMappings.keySet()){
			if(listenerToEventsMappings.get(listener).contains(eventType)){
				Method method = eventToMethodMappings.get(eventType);
				notifyListener(listener, method, event);
			}
		}
	}
	private void notifyListener(Rdp4jListener listener, Method method, Event event) {
		try {
			method.invoke(listener, event);
		} catch (Throwable t) {
			// Ignore if a listener crashes. Continue with next listener.
			// Don't let one listener ruin for other listeners... 
			String message = "Method '%s' not invokable with parameter '%s' on listener '%s'";
			logger.error(String.format(message, method, event, listener), t);
		}
	}

}
