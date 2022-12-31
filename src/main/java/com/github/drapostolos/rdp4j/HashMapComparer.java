package com.github.drapostolos.rdp4j;

import java.util.HashMap;
import java.util.Map;

final class HashMapComparer<K, V> {

    private Map<K, V> oldMap, newMap, added, removed;

    HashMapComparer(Map<K, V> oldMap, Map<K, V> newMap) {
        this.oldMap = oldMap;
        this.newMap = newMap;
        initAdded();
        initRemoved();
    }

    private void initAdded() {
        added = new HashMap<K, V>(newMap);
        for (K key : oldMap.keySet()) {
            added.remove(key);
        }
    }

    private void initRemoved() {
        removed = new HashMap<K, V>(oldMap);
        for (K key : newMap.keySet()) {
            removed.remove(key);
        }
    }

    /**
     * Returns the element that are in newMap, but not in oldMap.
     * 
     * @return
     */
    Map<K, V> getAdded() {
        return added;
    }

    /**
     * Returns the element that are in oldMap, but not in newMap.
     * 
     * @return
     */
    Map<K, V> getRemoved() {
        return removed;
    }

    /**
     * @return
     */
    boolean hasDiff() {
        return !oldMap.equals(newMap);
    }

    @Override
    public String toString() {
        return "Removed: " + getRemoved() + ", Added: " + getAdded();
    }
}
