package org.example.cache;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.example.cache.eviction.FifoEvictionStrategy;

public class GenericConcurrentCacheBenchmarkTest {
    @Test
    @Disabled("Benchmark manuel — ne pas exécuter automatiquement")
    void benchmarkPutThroughput() {
        Cache<String, String> cache =
                new GenericConcurrentCache<>(100_000, 0, new FifoEvictionStrategy<>());

        int ops = 100_000;
        long start = System.nanoTime();

        for (int i = 0; i < ops; i++) {
            cache.put("key" + i, "value" + i);
        }

        long duration = System.nanoTime() - start;

        double opsPerSec = (ops * 1_000_000_000.0) / duration;
        System.out.println("Throughput: " + opsPerSec + " ops/sec");
    }
}
