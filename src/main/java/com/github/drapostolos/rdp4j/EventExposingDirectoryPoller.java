package com.github.drapostolos.rdp4j;

abstract class EventExposingDirectoryPoller implements Event {

    final DirectoryPoller dp;

    EventExposingDirectoryPoller(DirectoryPoller directoryPoller) {
        dp = directoryPoller;
    }

    /**
     * Returns the {@link DirectoryPoller} instance which fired
     * this event.
     * 
     * @return {@link DirectoryPoller}
     */
    public DirectoryPoller getDirectoryPoller() {
        return dp;
    }

}
