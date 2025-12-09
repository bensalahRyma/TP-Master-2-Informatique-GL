// File: src/main/java/com/example/cache/eviction/FifoEvictionStrategy.java
package com.example.cache.eviction;

import org.example.cache.eviction.EvictionStrategy;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Simple FIFO eviction strategy: the earliest inserted key is evicted first.
 */
public class FifoEvictionStrategy<K> implements EvictionStrategy<K> {

    private final Queue<K> queue = new ConcurrentLinkedQueue<>();
    private final LinkedHashSet<K> present = new LinkedHashSet<>();

    @Override
    public synchronized void onGet(K key) {
        // No-op for FIFO
    }

    @Override
    public synchronized void onPut(K key) {
        if (!present.contains(key)) {
            present.add(key);
            queue.offer(key);
        }
    }

    @Override
    public synchronized void onRemove(K key) {
        present.remove(key);
        // queue will naturally skip removed keys in selectKeyToEvict
    }

    @Override
    public synchronized Optional<K> selectKeyToEvict() {
        while (true) {
            K key = queue.poll();
            if (key == null) {
                return Optional.empty();
            }
            if (present.remove(key)) {
                return Optional.of(key);
            }
        }
    }
}
