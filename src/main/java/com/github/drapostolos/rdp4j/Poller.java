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

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

class Poller implements Callable<Object> {

    private static Logger logger = LoggerFactory.getLogger(ScheduledRunnable.class);
    private final List<CachedFileElement> modifiedFiles = new ArrayList<CachedFileElement>();
	private final FileFilter filter;
	private final ListenerNotifier notifier;
    private boolean isFirstPollCycle = true;
	private boolean isFileSystemUnaccessible = true;
    private HashMapComparer<String, CachedFileElement> mapComparer;
    private final Map<String, CachedFileElement> currentListedFiles;
    private final Map<String, CachedFileElement> previousListedFiles;
    private final PolledDirectory directory;
    private final DirectoryPoller dp;

    Poller(DirectoryPoller dp, PolledDirectory directory) {
        this(dp, directory, Util.newLinkedHashMap(), Util.newLinkedHashMap());
    }

    /*
     * Use this constructor when letting client provide snapshot of last poll,
     * from a previous DirectoryPoller.
     */
    private Poller(DirectoryPoller dp, PolledDirectory directory,
            Map<String, CachedFileElement> currentListedFiles,
            Map<String, CachedFileElement> previousListedFiles) {
		this.dp = dp;
		this.directory = directory;
		this.filter = dp.getDefaultFileFilter();
		this.notifier = dp.notifier;
        this.currentListedFiles = currentListedFiles;
        this.previousListedFiles = previousListedFiles;
	}

    @Override
    public Object call() {
		collectCurrentFilesAndNotifyListenersIfIoErrorRaisedOrCeased();
		if(isFilesystemAccessible()){
			detectAndCollectModifiedFiles();
			setComparerForCurrentVersusPreviousListedFiles();
            if (isFirstPollCycle) {
                doActionsSpecificForFirstPollCycle();
                isFirstPollCycle = false;
            } else {
                doActionsForRemainingPollCycles();
            }
            if (isDirectoryModified()) {
                copyCurrentListedFilesToPrevious();
            }
		}
		return null;
	}

    private void doActionsSpecificForFirstPollCycle() {
        if (dp.fileAddedEventEnabledForInitialContent) {
            // make sure this events fires before InitialContentEvent
            notifyListenersWithRemovedAddedModifiedFiles();
        }
        Event event = new InitialContentEvent(dp, directory, currentListedFiles);
        dp.notifier.notifyListeners(event);
    }

    private void doActionsForRemainingPollCycles() {
        notifyListenersWithRemovedAddedModifiedFiles();
    }

    private boolean isFilesystemAccessible() {
        return isFileSystemUnaccessible;
    }

    private void setComparerForCurrentVersusPreviousListedFiles() {
        mapComparer = new HashMapComparer<String, CachedFileElement>(previousListedFiles, currentListedFiles);
    }

	private void detectAndCollectModifiedFiles() {
		modifiedFiles.clear();
        for (CachedFileElement f : currentListedFiles.values()) {
			if(isFileModified(f)){
				modifiedFiles.add(f);
			}
		}
	}

    private boolean isFileModified(CachedFileElement file) {
        if (previousListedFiles.containsKey(file.getName())) {
            long current = file.lastModified();
            long previous = previousListedFiles.get(file.getName()).lastModified();
			return current != previous;
		}
		return false;
	}

	private void collectCurrentFilesAndNotifyListenersIfIoErrorRaisedOrCeased(){
		try {
			Set<FileElement> files = directory.listFiles();
			if(files == null){
				String message = new StringBuilder()
				.append("Unknown underlying IO-error when listing files ")
				.append("in directory: '%s'. Method listFiles() returned null.")
				.toString();
				throw new IOException(String.format(message, directory));
			}
            Map<String, CachedFileElement> temp = new LinkedHashMap<String, CachedFileElement>();
			for(FileElement file : files){
				if(filter.accept(file)){
					long lastModified = file.lastModified();
					if(lastModified == 0L){
						throw new IOException("Unknown underlying IO-Error. Method 'lastModified()' returned '0L' for file '" + file + "'");
					}
					String name = file.getName();
                    temp.put(name, new CachedFileElement(file, name, lastModified));
				}
			}
			if(isFilesystemUnaccessible()){
				notifier.notifyListeners(new IoErrorCeasedEvent(dp, directory));
				isFileSystemUnaccessible = true;
			}
			currentListedFiles.clear();
			currentListedFiles.putAll(temp);
		} catch (IOException e) {
			if(isFilesystemAccessible()){
				isFileSystemUnaccessible = false;
				notifier.notifyListeners(new IoErrorRaisedEvent(dp, directory, e));
			}
		} catch(DirectoryPollerException e){
			// Silently wait fore next poll.
        } catch (RuntimeException e) {
            dp.stopAsync();
			String message = 
                    "DirectoryPoller will be stopped "
			+ "due to unexpected crash in PolledDirectory implementation '%s'. "
                            + "See underlying exception for more information.";
			message = String.format(message, directory.getClass().getName());
			logger.error(message, e);
			throw new IllegalStateException(message, e);
		}
	}

    private boolean isFilesystemUnaccessible() {
		return !isFilesystemAccessible();
	}

    private boolean isDirectoryModified() {
		return !modifiedFiles.isEmpty() || mapComparer.hasDiff();
	}

    private void notifyListenersWithRemovedAddedModifiedFiles() {
        for (CachedFileElement file : mapComparer.getRemoved().values()) {
			notifier.notifyListeners(new FileRemovedEvent(dp, directory, file.fileElement));
		}
        for (CachedFileElement file : mapComparer.getAdded().values()) {
			notifier.notifyListeners(new FileAddedEvent(dp, directory, file.fileElement));
		}
        for (CachedFileElement file : modifiedFiles) {
			notifier.notifyListeners(new FileModifiedEvent(dp, directory, file.fileElement));
		}
	}

    private void copyCurrentListedFilesToPrevious() {
        previousListedFiles.clear();
        previousListedFiles.putAll(currentListedFiles);
    }

}
