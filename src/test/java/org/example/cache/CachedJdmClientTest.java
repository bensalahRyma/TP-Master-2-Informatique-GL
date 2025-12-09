package org.example.cache;

import com.example.cache.eviction.FifoEvictionStrategy;
import org.example.jdm.CachedJdmClient;
import org.example.jdm.JdmClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CachedJdmClientTest {

    private static class FakeJdmClient extends JdmClient {
        private int calls = 0;

        @Override
        public String getTermRaw(String term) {
            calls++;
            return "{\"term\":\"" + term + "\"}";
        }

        public int getCalls() {
            return calls;
        }
    }

    private static class SimpleStringCache implements Cache<String, String> {

        private final java.util.Map<String, String> map = new java.util.HashMap<>();
        private CacheStats stats = new CacheStats(0, 0, 0);

        @Override
        public String get(String key) {
            if (map.containsKey(key)) {
                stats = stats.addHit();
                return map.get(key);
            } else {
                stats = stats.addMiss();
                return null;
            }
        }

        @Override
        public void put(String key, String value) {
            map.put(key, value);
            stats = stats.addPut();
        }

        @Override
        public void invalidate(String key) {
            map.remove(key);
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public CacheStats getStats() {
            return stats;
        }
    }

    @Test
    void testCachedClientUsesCache() {
        FakeJdmClient jdmClient = new FakeJdmClient();
        Cache<String, String> cache =
                new GenericConcurrentCache<>(10, 0, new FifoEvictionStrategy<>());
        CachedJdmClient cached = new CachedJdmClient(jdmClient, cache);

        String t1 = cached.getTermRaw("chat");
        String t2 = cached.getTermRaw("chat");

        assertEquals(t1, t2);
        assertEquals(1, jdmClient.getCalls());
        assertTrue(cache.getStats().hits() >= 1);
    }

    @Test
    void testCachedClientWithSimpleCache() {
        FakeJdmClient jdmClient = new FakeJdmClient();
        Cache<String, String> cache = new SimpleStringCache();
        CachedJdmClient cached = new CachedJdmClient(jdmClient, cache);

        String t1 = cached.getTermRaw("chien");
        String t2 = cached.getTermRaw("chien");

        assertEquals(t1, t2);
        assertEquals(1, jdmClient.getCalls());
        CacheStats stats = cache.getStats();
        assertEquals(1, stats.hits());
        assertEquals(1, stats.misses());
    }
}
