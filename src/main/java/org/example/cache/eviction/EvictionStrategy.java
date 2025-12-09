package org.example.cache.eviction;

import java.util.Optional;

/**
 * Strategy interface used by the cache to decide which key to evict.
 *
 * @param <K> key type
 */
public interface EvictionStrategy<K> {

    /**
     * Called when a key is accessed (e.g., get).
     */
    void onGet(K key);

    /**
     * Called when a key is inserted or updated.
     */
    void onPut(K key);

    /**
     * Called when a key is removed from the cache.
     */
    void onRemove(K key);

    /**
     * Selects a key to evict, if any.
     *
     * @return an optional key to evict
     */
    Optional<K> selectKeyToEvict();
}