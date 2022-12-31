package com.github.drapostolos.rdp4j;

import com.github.drapostolos.rdp4j.spi.Persister;

class StatePersister implements DirectoryPollerListener {
	private final Persister persister;
	
	StatePersister(Persister persister) {
		this.persister = persister;
	}

	@Override
	public void beforeStart(BeforeStartEvent event) {
		if(persister.containsData()) {
			persister.readData().entrySet().forEach(entry -> {
				event.addPolledDirectory(entry.getKey(), entry.getValue());
			});
		}
	}

	@Override
	public void afterStop(AfterStopEvent event) {
		persister.writeData(event.getCachedFileElements());
	}

}
