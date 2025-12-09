package org.example.cache;



import org.example.cache.eviction.EvictionStrategy;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Cache générique concurrent implémentant plusieurs stratégies d’éviction.
 *
 * <p>Caractéristiques :
 * - Thread-safe (ConcurrentHashMap + sections critiques minimales)
 * - Performant : opérations get/put en O(1)
 * - Extensible via un pattern Strategy (EvictionStrategy)
 * - Support optionnel du TTL
 *
 * @param <K> type de la clé
 * @param <V> type de la valeur
 */
public final class GenericConcurrentCache<K, V> implements Cache<K, V> {

    private final ConcurrentHashMap<K, CacheEntry<V>> store;
    private final EvictionStrategy<K> evictionStrategy;
    private final int maxSize;
    private final long ttlNanos;
    private final boolean useTtl;

    // Lock dédié uniquement à l’éviction (et non à tous les puts)
    private final ReentrantLock evictionLock = new ReentrantLock();

    // Stats avec LongAdder pour limiter la contention
    private final LongAdder hits = new LongAdder();
    private final LongAdder misses = new LongAdder();

    public GenericConcurrentCache(int maxSize, long ttlMillis, EvictionStrategy<K> evictionStrategy) {
        if (maxSize <= 0) throw new IllegalArgumentException("maxSize must be > 0");
        this.maxSize = maxSize;
        this.ttlNanos = ttlMillis > 0 ? TimeUnit.MILLISECONDS.toNanos(ttlMillis) : 0L;
        this.useTtl = ttlMillis > 0;
        this.evictionStrategy = Objects.requireNonNull(evictionStrategy);
        // Pré-dimensionnement pour limiter les réallocations
        this.store = new ConcurrentHashMap<>(maxSize * 2);
    }

    /**
     * @pre key != null
     * @post renvoie la valeur associée ou null si absente ou expirée
     * @post incrémente les statistiques (hit/miss)
     */
    @Override
    public V get(K key) {
        Objects.requireNonNull(key, "key must not be null");
        CacheEntry<V> entry = store.get(key);
        if (entry == null) {
            misses.increment();
            return null;
        }
        if (useTtl && entry.isExpired()) {
            // Nettoyage paresseux
            invalidate(key);
            misses.increment();
            return null;
        }
        hits.increment();
        evictionStrategy.onGet(key);
        return entry.value();
    }

    /**
     * @pre key != null && value != null
     * @post le cache contient la paire (key, value)
     * @post peut déclencher une éviction en fonction de la stratégie
     */
    @Override
    public void put(K key, V value) {
        Objects.requireNonNull(key, "key must not be null");
        long expiry = useTtl ? System.nanoTime() + ttlNanos : CacheEntry.NO_EXPIRY;
        store.put(key, new CacheEntry<>(value, expiry));
        evictionStrategy.onPut(key);
        enforceCapacityIfNeeded();
    }

    private void enforceCapacityIfNeeded() {
        // Check rapide, sans lock
        if (store.size() <= maxSize) return;

        // On essaie de prendre le lock; si on échoue, un autre thread est déjà en train d'évincer
        if (!evictionLock.tryLock()) {
            return;
        }
        try {
            while (store.size() > maxSize) {
                evictionStrategy
                        .selectKeyToEvict()
                        .ifPresentOrElse(
                                store::remove,
                                () -> { /* plus rien à évincer, on sort */ });
            }
        } finally {
            evictionLock.unlock();
        }
    }

    @Override
    public void invalidate(K key) {
        Objects.requireNonNull(key, "key must not be null");
        store.remove(key);
        evictionStrategy.onRemove(key);
    }

    @Override
    public void clear() {
        store.clear();
        // Éventuellement : réinitialiser la stratégie si besoin
    }

    @Override
    public CacheStats getStats() {
        long h = hits.sum();
        long m = misses.sum();
        long total = h + m;
        double hitRate = total == 0 ? 0.0 : (double) h / total;
        return new CacheStats(h, m, (long) hitRate);
    }

    // CacheEntry record interne
    private record CacheEntry<V>(V value, long expiryNanos) {
        static final long NO_EXPIRY = -1L;

        boolean isExpired() {
            return expiryNanos != NO_EXPIRY && System.nanoTime() > expiryNanos;
        }
    }
}
