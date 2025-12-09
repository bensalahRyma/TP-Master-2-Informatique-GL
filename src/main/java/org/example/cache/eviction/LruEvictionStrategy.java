package org.example.cache.eviction;

import org.example.cache.eviction.EvictionStrategy;

import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * Implémentation de la stratégie LRU (Least Recently Used) basée sur LinkedHashMap.
 *
 * <p>Cette implémentation garantit l'accès et la mise à jour de l’ordre LRU en O(1).
 */
public final class LruEvictionStrategy<K> implements EvictionStrategy<K> {

    // LinkedHashMap with accessOrder=true → automatic LRU ordering
    private final LinkedHashMap<K, Boolean> access =
            new LinkedHashMap<>(16, 0.75f, true);

    @Override
    public synchronized void onGet(K key) {
        if (access.containsKey(key)) {
            access.get(key); // reorder key
        }
    }

    @Override
    public synchronized void onPut(K key) {
        access.put(key, Boolean.TRUE);
    }

    @Override
    public synchronized void onRemove(K key) {
        access.remove(key);
    }

    @Override
    public synchronized Optional<K> selectKeyToEvict() {
        if (access.isEmpty()) {
            return Optional.empty();
        }

        var iterator = access.entrySet().iterator();
        var eldest = iterator.next().getKey();
        iterator.remove();

        return Optional.of(eldest);
    }
}