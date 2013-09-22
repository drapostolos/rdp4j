package com.github.drapostolos.rdp4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.drapostolos.adp4j.spi.FileElement;
import com.github.drapostolos.adp4j.spi.PolledDirectory;

class Poller implements Callable<Object>{
	private static Logger logger = LoggerFactory.getLogger(PollerTask.class);
	private final Map<String, FileElementCacher> currentListedFiles = new LinkedHashMap<String, FileElementCacher>();
	private final Map<String, FileElementCacher> previousListedFiles = new LinkedHashMap<String, FileElementCacher>();
	private final List<FileElementCacher> modifiedFiles = new ArrayList<FileElementCacher>();
	private final DirectoryPoller dp;
	private final PolledDirectory directory;
	private final FileFilter filter;
	private final ListenerNotifier notifier;
	private final boolean fileAddedEventEnabledForInitialContent;
	private boolean isSecondPollCycleOrLater = false;
	private boolean isFilesystemUnaccessible = true;
	private HashMapComparer<String, FileElementCacher> mapComparer;

	Poller(DirectoryPoller dp, PolledDirectory directory) {
		this.dp = dp;
		this.directory = directory;
		this.filter = dp.getDefaultFileFilter();
		this.notifier = dp.notifier;
		this.fileAddedEventEnabledForInitialContent = dp.fileAddedEventEnabledForInitialContent;
	}

	public Object call() {
		listCurrentFilesAndNotifyListenersIfIoErrorRaisedOrCeased();
		if(isFilesystemAccessible()){
			updateModifiedFiles();
			setComparerForCurrentVersusPreviousListedFiles();
			if(isDirectoryModified()){
				if(isSecondPollCycleOrLater() || fileAddedEventEnabledForInitialContent){
					notifyListenersWithRemovedAddedModifiedFiles();
				}
				copyCurrentListedFilesToPrevious();
			}
			if(isFirstPollCycle() ){
				// Only do this once, i.e. the first time files are listed successfully. 
				Set<FileElement> files = FileElementCacher.toFileElements(currentListedFiles);
				notifier.notifyListeners(new InitialContentEvent(dp, directory, files));
				isSecondPollCycleOrLater = true;
			}
		}
		return null;
	}

	boolean isSecondPollCycleOrLater(){
		return isSecondPollCycleOrLater;
	}
	boolean isFirstPollCycle(){
		return !isSecondPollCycleOrLater();
	}

	private void updateModifiedFiles() {
		modifiedFiles.clear();
		for(FileElementCacher f : currentListedFiles.values()){
			if(isFileModified(f)){
				modifiedFiles.add(f);
			}
		}
	}
	private boolean isFileModified(FileElementCacher file) {
		if(previousListedFiles.containsKey(file.name)){
			long current = file.lastModified;
			long previous = previousListedFiles.get(file.name).lastModified;
			return current != previous;
		}
		return false;
	}

	private void listCurrentFilesAndNotifyListenersIfIoErrorRaisedOrCeased(){
		try {
			Set<FileElement> files = directory.listFiles();
			if(files == null){
				String message = new StringBuilder()
				.append("Unknown underlying IO-error when listing files ")
				.append("in directory: '%s'. Method listFiles() returned null.")
				.toString();
				throw new IOException(String.format(message, directory));
			}
			Map<String, FileElementCacher> temp = new LinkedHashMap<String, FileElementCacher>();
			for(FileElement file : files){
				if(filter.accept(file)){
					long lastModified = file.lastModified();
					if(lastModified == 0L){
						throw new IOException("Unknown underlying IO-Error. Method 'lastModified()' returned '0L' for file '" + file + "'");
					}
					String name = file.getName();
					temp.put(name, new FileElementCacher(file, name, lastModified));
				}
			}
			if(isFilesystemUnaccessible()){
				notifier.notifyListeners(new IoErrorCeasedEvent(dp, directory));
				isFilesystemUnaccessible = true;
			}
			currentListedFiles.clear();
			currentListedFiles.putAll(temp);
		} catch (IOException e) {
			if(isFilesystemAccessible()){
				isFilesystemUnaccessible = false;
				notifier.notifyListeners(new IoErrorRaisedEvent(dp, directory, e));
			}
		} catch(DirectoryPollerException e){
			// Silently wait fore next poll.
		} catch (Throwable e){
			dp.timer.cancel();
			String message = 
			"DirectoryPoller is stopped (effectively after current poll-cycle) "
			+ "due to unexpected crash in PolledDirectory implementation '%s'. "
			+ "See underlying exception for more info.";
			message = String.format(message, directory.getClass().getName());
			logger.error(message, e);
			throw new IllegalStateException(message, e);
		}
	}

	boolean isFilesystemAccessible(){
		return isFilesystemUnaccessible;
	}
	boolean isFilesystemUnaccessible(){
		return !isFilesystemAccessible();
	}

	private void setComparerForCurrentVersusPreviousListedFiles(){
		mapComparer = new HashMapComparer<String, FileElementCacher>(previousListedFiles, currentListedFiles);
	}

	private boolean isDirectoryModified() {
		return !modifiedFiles.isEmpty() || mapComparer.hasDiff();
	}

	private void notifyListenersWithRemovedAddedModifiedFiles(){
		for(FileElementCacher file : mapComparer.getRemoved().values()){
			notifier.notifyListeners(new FileRemovedEvent(dp, directory, file.fileElement));
		}

		for(FileElementCacher file : mapComparer.getAdded().values()){
			notifier.notifyListeners(new FileAddedEvent(dp, directory, file.fileElement));
		}
		for(FileElementCacher file : modifiedFiles){
			notifier.notifyListeners(new FileModifiedEvent(dp, directory, file.fileElement));
		}
	}

	private void copyCurrentListedFilesToPrevious(){
		previousListedFiles.clear();
		previousListedFiles.putAll(currentListedFiles);
	}



}
