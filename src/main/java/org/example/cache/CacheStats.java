package org.example.cache;

import java.util.Objects;

/**
 * Immutable statistics for a cache instance.
 */
public final class CacheStats {

    private final long hits;
    private final long misses;
    private final long puts;

    public CacheStats(long hits, long misses, long puts) {
        this.hits = hits;
        this.misses = misses;
        this.puts = puts;
    }

    public long hits() {
        return hits;
    }

    public long misses() {
        return misses;
    }

    public long puts() {
        return puts;
    }

    public long requests() {
        return hits + misses;
    }

    public double hitRate() {
        long requests = requests();
        if (requests == 0) {
            return 0.0;
        }
        return (double) hits / (double) requests;
    }

    public double missRate() {
        long requests = requests();
        if (requests == 0) {
            return 0.0;
        }
        return (double) misses / (double) requests;
    }

    public CacheStats addHit() {
        return new CacheStats(hits + 1, misses, puts);
    }

    public CacheStats addMiss() {
        return new CacheStats(hits, misses + 1, puts);
    }

    public CacheStats addPut() {
        return new CacheStats(hits, misses, puts + 1);
    }

    @Override
    public String toString() {
        return "CacheStats{" +
                "hits=" + hits +
                ", misses=" + misses +
                ", puts=" + puts +
                ", hitRate=" + hitRate() +
                ", missRate=" + missRate() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheStats)) return false;
        CacheStats that = (CacheStats) o;
        return hits == that.hits &&
                misses == that.misses &&
                puts == that.puts;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hits, misses, puts);
    }
}