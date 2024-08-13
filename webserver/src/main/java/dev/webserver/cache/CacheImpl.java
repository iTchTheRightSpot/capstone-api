package dev.webserver.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class CacheImpl<K, V> {

    private final Cache<K, V> cache;

    /**
     * Creates an instance of {@link CacheImpl}.
     *
     * @param time how long items in the cache should be valid for in minutes.
     * @param size determines the max size of the in memory cache.
     * */
    public CacheImpl(final int time, final int size) {
        cache = CacheBuilder.newBuilder()
                .maximumSize(size)
                .expireAfterWrite(time, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Retrieve value if exists or null.
     */
    public Optional<V> getIfPresent(final K key) {
        final V value = cache.getIfPresent(key);
        return Optional.ofNullable(value);
    }

    /**
     * Add or update Cache.
     */
    public void put(final K key, final V value) {
        cache.put(key, value);
    }

    /**
     * Invalidate item from cache.
     */
    public void evict(final K key) {
        cache.invalidate(key);
    }

    /**
     * Clears out the cache.
     * */
    public void evictAll() {
        cache.invalidateAll();
    }

    public Set<Map.Entry<K, V>> entryset() {
        return cache.asMap().entrySet();
    }

}
