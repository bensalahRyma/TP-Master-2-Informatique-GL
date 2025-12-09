package org.example.jdm;
import org.example.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around JdmClient that caches responses using a generic cache.
 */
public class CachedJdmClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachedJdmClient.class);

    private final JdmClient jdmClient;
    private Cache<String, String> cache;

    public CachedJdmClient(JdmClient jdmClient, Cache<String, String> cache) {
        this.jdmClient = jdmClient;
        this.cache = cache;
    }

    public void setCache(Cache<String, String> cache) {
        this.cache = cache;
    }

    public String getTermRaw(String term) {
        return getWithCache("term:" + term, () -> jdmClient.getTermRaw(term));
    }

    public String getRelationsRaw(String term) {
        return getWithCache("relations:" + term, () -> jdmClient.getRelationsRaw(term));
    }

    public String getSynonymsRaw(String term) {
        return getWithCache("syn:" + term, () -> jdmClient.getSynonymsRaw(term));
    }

    public String getAntonymsRaw(String term) {
        return getWithCache("anto:" + term, () -> jdmClient.getAntonymsRaw(term));
    }

    public String getAssociationsRaw(String term) {
        return getWithCache("assoc:" + term, () -> jdmClient.getAssociationsRaw(term));
    }

    private String getWithCache(String key, SupplierWithException supplier) {
        long start = System.nanoTime();
        String cached = cache.get(key);
        if (cached != null) {
            long duration = System.nanoTime() - start;
            LOGGER.info("Cache HIT for key={}, duration={}µs", key, duration / 1_000);
            return cached;
        }
        String value;
        try {
            value = supplier.get();
        } catch (Exception e) {
            throw new JdmApiException("Error fetching JDM data", e);
        }
        cache.put(key, value);
        long duration = System.nanoTime() - start;
        LOGGER.info("Cache MISS for key={}, duration={}µs (including network)", key, duration / 1_000);
        return value;
    }

    @FunctionalInterface
    private interface SupplierWithException {
        String get() throws Exception;
    }
}