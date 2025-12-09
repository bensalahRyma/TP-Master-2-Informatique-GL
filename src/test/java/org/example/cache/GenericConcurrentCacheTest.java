package org.example.cache;

import com.example.cache.eviction.FifoEvictionStrategy;

import org.example.cache.eviction.LruEvictionStrategy;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class GenericConcurrentCacheTest {

    @Test
    void testPutAndGet() {
        Cache<String, String> cache =
                new GenericConcurrentCache<>(10, 0, new FifoEvictionStrategy<>());

        cache.put("a", "1");
        cache.put("b", "2");

        assertEquals("1", cache.get("a"));
        assertEquals("2", cache.get("b"));
        assertNull(cache.get("c"));

        CacheStats stats = cache.getStats();
        assertEquals(2, stats.hits());
        assertEquals(1, stats.misses());
    }

    @Test
    void testInvalidate() {
        Cache<String, String> cache =
                new GenericConcurrentCache<>(10, 0, new FifoEvictionStrategy<>());

        cache.put("a", "1");
        assertEquals("1", cache.get("a"));

        cache.invalidate("a");
        assertNull(cache.get("a"));
    }

    @Test
    void testClear() {
        Cache<String, String> cache =
                new GenericConcurrentCache<>(10, 0, new FifoEvictionStrategy<>());

        cache.put("a", "1");
        cache.put("b", "2");

        cache.clear();

        assertNull(cache.get("a"));
        assertNull(cache.get("b"));
    }

    @Test
    void testEvictionWithFifo() {
        int maxSize = 3;
        Cache<String, String> cache =
                new GenericConcurrentCache<>(maxSize, 0, new FifoEvictionStrategy<>());

        cache.put("k1", "v1");
        cache.put("k2", "v2");
        cache.put("k3", "v3");
        cache.put("k4", "v4"); // should evict one

        int present = 0;
        for (String key : new String[]{"k1", "k2", "k3", "k4"}) {
            if (cache.get(key) != null) {
                present++;
            }
        }
        assertEquals(maxSize, present);
    }

    @Test
    void testLruEvictionOrder() {
        int maxSize = 2;

        Cache<String, String> cache =
                new GenericConcurrentCache<>(maxSize, 0, new LruEvictionStrategy<>());

        cache.put("A", "1");
        cache.put("B", "2");

        // Access A to make it most recently used
        assertEquals("1", cache.get("A"));

        // Insert C → this should evict B
        cache.put("C", "3");

        assertNotNull(cache.get("A"), "A should NOT be evicted (it was recently used)");
        assertNull(cache.get("B"), "B should be evicted (least recently used)");
        assertNotNull(cache.get("C"), "C must be present");
    }


    @Test
    void testTtlExpiration() throws InterruptedException {
        long ttlMillis = 200;
        Cache<String, String> cache =
                new GenericConcurrentCache<>(10, ttlMillis, new FifoEvictionStrategy<>());

        cache.put("temp", "value");
        assertEquals("value", cache.get("temp"));

        Thread.sleep(ttlMillis + 100);

        assertNull(cache.get("temp"));
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        int maxSize = 100;
        Cache<Integer, String> cache =
                new GenericConcurrentCache<>(maxSize, 0, new FifoEvictionStrategy<>());

        ExecutorService executor = Executors.newFixedThreadPool(16);
        int tasks = 1000;

        for (int i = 0; i < tasks; i++) {
            final int key = i % 50;
            executor.submit(() -> {
                cache.put(key, "v" + key);
                cache.get(key);
            });
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);
        assertTrue(finished);

        Set<Integer> keys = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            String value = cache.get(i);
            if (value != null) {
                keys.add(i);
            }
        }
        assertFalse(keys.isEmpty());
    }
    @Test
    void concurrentPutAndGet_shouldNotThrowOrCorruptState() throws InterruptedException {
        int maxSize = 100;
        Cache<String, String> cache =
                new GenericConcurrentCache<>(maxSize, 0, new FifoEvictionStrategy<>());

        int threads = 50;
        int operationsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await(); // start all threads at the same time
                    for (int i = 0; i < operationsPerThread; i++) {
                        int k = i % 10; // collisions volontaires
                        String key = "t" + threadId + "-k" + k;
                        cache.put(key, "value-" + threadId + "-" + k);
                        String v = cache.get(key);
                        // On ne fait pas d'assert ici pour éviter les ralentissements
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finished = doneLatch.await(20, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished, "Les threads n'ont pas fini à temps (risque de deadlock)");
    }

    @Test
    void getOnEmptyCache_shouldReturnNullAndCountMiss() {
        Cache<String, String> cache =
                new GenericConcurrentCache<>(10, 0, new FifoEvictionStrategy<>());

        assertNull(cache.get("x"));
        assertEquals(1, cache.getStats().misses());
        assertEquals(0, cache.getStats().hits());
    }
    //Collision de clés : écrasement de valeurs
    @Test
    void putOnExistingKey_shouldOverwriteValue() {
        Cache<String, String> cache =
                new GenericConcurrentCache<>(10, 0, new FifoEvictionStrategy<>());

        cache.put("k", "v1");
        cache.put("k", "v2");

        assertEquals("v2", cache.get("k"));
    }



}