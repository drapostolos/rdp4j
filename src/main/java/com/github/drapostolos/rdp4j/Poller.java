package com.github.drapostolos.rdp4j;

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

class Poller implements Callable<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduledRunnable.class);
    final PolledDirectory directory;
    private final List<FileElementAndCache> modifiedFiles = new ArrayList<>();
    private final FileFilter filter;
    private final ListenerNotifier notifier;
    private boolean isFirstPollCycle = true;
    private boolean isFileSystemAccessible = true; 
    private HashMapComparer<String, FileElementAndCache> mapComparer;
    final Map<String, FileElementAndCache> currentListedFiles;
    private final Map<String, FileElementAndCache> previousListedFiles;
    private final DirectoryPoller dp;

    Poller(DirectoryPoller dp, PolledDirectory directory, Set<CachedFileElement> previousListedFiles) {
        this.dp = dp;
        this.directory = directory;
        this.filter = dp.getDefaultFileFilter();
        this.notifier = dp.notifier;
        this.currentListedFiles = new LinkedHashMap<>();
        this.previousListedFiles = previousListedFiles.stream()
        		.map(file -> new FileElementAndCache(file, file))
        		.collect(toMap(e ->e.getName(), e -> e, (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Override
    public Object call() throws InterruptedException {
        collectCurrentFilesAndNotifyListenersIfIoErrorRaisedOrCeased();
        if (isFilesystemAccessible()) {
            detectAndCollectModifiedFiles();
            setComparerForCurrentVersusPreviousListedFiles();
            if (isFirstPollCycle) {
                doActionsSpecificForFirstPollCycle();
                isFirstPollCycle = false;
            } else {
                notifyListenersWithRemovedAddedModifiedFiles();
            }
            if (isDirectoryModified()) {
                copyCurrentListedFilesToPrevious();
            }
        }
        return null;
    }

    private void doActionsSpecificForFirstPollCycle() throws InterruptedException {
        if (dp.fileAddedEventEnabledForInitialContent) {
            // make sure this events fires before InitialContentEvent
            notifyIfNeeded(notifier::fileAdded, file -> new FileAddedEvent(dp, directory, file), currentListedFiles.values());
        }
        dp.notifier.initialContent(new InitialContentEvent(dp, directory, currentListedFiles.values()));
        if(!previousListedFiles.isEmpty()) {
        	notifyListenersWithRemovedAddedModifiedFiles();
        }
    }

    private boolean isFilesystemAccessible() {
        return isFileSystemAccessible;
    }

    private void setComparerForCurrentVersusPreviousListedFiles() {
        mapComparer = new HashMapComparer<>(previousListedFiles, currentListedFiles);
    }

    private void detectAndCollectModifiedFiles() {
        modifiedFiles.clear();
        for (FileElementAndCache f : currentListedFiles.values()) {
            if (isFileModified(f)) {
                modifiedFiles.add(f);
            }
        }
    }

    private boolean isFileModified(FileElementAndCache file) {
        if (previousListedFiles.containsKey(file.getName())) {
            long current = file.lastModified();
            long previous = previousListedFiles.get(file.getName()).lastModified();
            return current != previous;
        }
        return false;
    }

    private void collectCurrentFilesAndNotifyListenersIfIoErrorRaisedOrCeased() throws InterruptedException {
        try {
            Set<FileElement> files = directory.listFiles();
            if (files == null) {
                String message = "Unknown underlying IO-error when listing files "
                        + "in directory: '%s'. Method listFiles() returned null.";
                throw new IOException(String.format(message, directory));
            }

            Map<String, FileElementAndCache> temp = filterFiles(files);
            if (isFilesystemUnaccessible()) {
                notifier.ioErrorCeased(new IoErrorCeasedEvent(dp, directory));
                isFileSystemAccessible = true;
            }
            currentListedFiles.clear();
            currentListedFiles.putAll(temp);
        } catch (IOException e) {
            if (isFilesystemAccessible()) {
                isFileSystemAccessible = false;
                notifier.ioErrorRaised(new IoErrorRaisedEvent(dp, directory, e));
            }
        } catch (DirectoryPollerException e) {
            // Silently wait fore next poll.
        } catch (RuntimeException e) {
            dp.stopAsync();
            String message = "DirectoryPoller will be stopped "
                    + "due to unexpected crash in PolledDirectory implementation '%s'. "
                    + "See underlying exception for more information.";
            message = String.format(message, directory.getClass().getName());
            LOG.error(message, e);
            throw new IllegalStateException(message, e);
        }
    }

    private Map<String, FileElementAndCache> filterFiles(Set<FileElement> files) throws IOException {
        Map<String, FileElementAndCache> result = new LinkedHashMap<>();
        for (FileElement file : files) {
            if (filter.accept(file)) {
            	FileElementAndCache cache = new FileElementAndCache(file, CachedFileElement.of(file));
                if (cache.lastModified() == 0L) {
                    String message = "Unknown underlying IO-Error. "
                            + "Method 'lastModified()' returned '0L' for file '%s'";
                    throw new IOException(format(message, file));
                }
                result.put(cache.getName(), cache);
            }
        }
        return result;
    }

    private boolean isFilesystemUnaccessible() {
        return !isFilesystemAccessible();
    }

    private boolean isDirectoryModified() {
        return !modifiedFiles.isEmpty() || mapComparer.hasDiff();
    }

    private void notifyListenersWithRemovedAddedModifiedFiles() throws InterruptedException {
        notifyIfNeeded(notifier::fileRemoved, file -> new FileRemovedEvent(dp, directory, file), mapComparer.getRemoved().values());
        notifyIfNeeded(notifier::fileAdded, file -> new FileAddedEvent(dp, directory, file), mapComparer.getAdded().values());
        notifyIfNeeded(notifier::fileModified, file -> new FileModifiedEvent(dp, directory, file), modifiedFiles);
    }
    
    private <T> void notifyIfNeeded(Notifier<T> notifier, Function<FileElementAndCache, T> event, Collection<FileElementAndCache> files) throws InterruptedException {
    	for (FileElementAndCache file : files) {
        	notifier.notify(event.apply(file));
        }
	}
    
    private interface Notifier<T> {
    	void notify(T event) throws InterruptedException;
    }

    private void copyCurrentListedFilesToPrevious() {
        previousListedFiles.clear();
        previousListedFiles.putAll(currentListedFiles);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((directory == null) ? 0 : directory.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Poller other = (Poller) obj;
        if (directory == null) {
            if (other.directory != null)
                return false;
        } else if (!directory.equals(other.directory))
            return false;
        return true;
    }

	PolledDirectory getPolledDirectory() {
		return directory;
	}

}
