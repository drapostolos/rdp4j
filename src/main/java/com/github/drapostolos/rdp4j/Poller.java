package com.github.drapostolos.rdp4j;

import static java.lang.String.format;

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

    private static final Logger LOG = LoggerFactory.getLogger(ScheduledRunnable.class);
    final PolledDirectory directory;
    private final List<CachedFileElement> modifiedFiles = new ArrayList<CachedFileElement>();
    private final FileFilter filter;
    private final ListenerNotifier notifier;
    private boolean isFirstPollCycle = true;
    private boolean isFileSystemAccessible = true; 
    private HashMapComparer<String, CachedFileElement> mapComparer;
    private final Map<String, CachedFileElement> currentListedFiles;
    private final Map<String, CachedFileElement> previousListedFiles;
    private final DirectoryPoller dp;

    Poller(DirectoryPoller dp, PolledDirectory directory) {
        this(dp, directory, Util.newLinkedHashMap(), Util.newLinkedHashMap());
    }

    /*
     * Use this constructor when letting client provide snapshot of last poll,
     * from a previous DirectoryPoller instance.
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
    public Object call() throws InterruptedException {
        collectCurrentFilesAndNotifyListenersIfIoErrorRaisedOrCeased();
        if (isFilesystemAccessible()) {
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

    private void doActionsSpecificForFirstPollCycle() throws InterruptedException {
        if (dp.fileAddedEventEnabledForInitialContent) {
            // make sure this events fires before InitialContentEvent
            notifyListenersWithRemovedAddedModifiedFiles();
        }
        dp.notifier.initialContent(new InitialContentEvent(dp, directory, currentListedFiles));
    }

    private void doActionsForRemainingPollCycles() throws InterruptedException {
        notifyListenersWithRemovedAddedModifiedFiles();
    }

    private boolean isFilesystemAccessible() {
        return isFileSystemAccessible;
    }

    private void setComparerForCurrentVersusPreviousListedFiles() {
        mapComparer = new HashMapComparer<String, CachedFileElement>(previousListedFiles, currentListedFiles);
    }

    private void detectAndCollectModifiedFiles() {
        modifiedFiles.clear();
        for (CachedFileElement f : currentListedFiles.values()) {
            if (isFileModified(f)) {
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

    private void collectCurrentFilesAndNotifyListenersIfIoErrorRaisedOrCeased() throws InterruptedException {
        try {
            Set<FileElement> files = directory.listFiles();
            if (files == null) {
                String message = "Unknown underlying IO-error when listing files "
                        + "in directory: '%s'. Method listFiles() returned null.";
                throw new IOException(String.format(message, directory));
            }

            Map<String, CachedFileElement> temp = filterFiles(files);
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

    private Map<String, CachedFileElement> filterFiles(Set<FileElement> files) throws IOException {
        Map<String, CachedFileElement> result = new LinkedHashMap<String, CachedFileElement>();
        for (FileElement file : files) {
            if (filter.accept(file)) {
                long lastModified = file.lastModified();
                if (lastModified == 0L) {
                    String message = "Unknown underlying IO-Error. "
                            + "Method 'lastModified()' returned '0L' for file '%s'";
                    throw new IOException(format(message, file));
                }
                String name = file.getName();
                result.put(name, new CachedFileElement(file, name, lastModified));
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
        for (CachedFileElement file : mapComparer.getRemoved().values()) {
            notifier.fileRemoved(new FileRemovedEvent(dp, directory, file.fileElement));
        }
        for (CachedFileElement file : mapComparer.getAdded().values()) {
            notifier.fileAdded(new FileAddedEvent(dp, directory, file.fileElement));
        }
        for (CachedFileElement file : modifiedFiles) {
            notifier.fileModified(new FileModifiedEvent(dp, directory, file.fileElement));
        }
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

}
