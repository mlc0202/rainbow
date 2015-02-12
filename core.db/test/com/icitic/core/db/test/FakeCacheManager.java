package com.icitic.core.db.test;

import com.icitic.core.cache.Cache;
import com.icitic.core.cache.CacheConfig;
import com.icitic.core.cache.CacheLoader;
import com.icitic.core.cache.CacheManager;
import com.icitic.core.cache.internal.MemoryCache;

public class FakeCacheManager implements CacheManager {

    @Override
    public <K, V> Cache<K, V> createCache(String name, CacheLoader<K, V> loader) {
        return createCache(name, loader, null);
    }

    @Override
    public <K, V> Cache<K, V> createCache(String name, CacheLoader<K, V> loader, CacheConfig config) {
        MemoryCache<K, V> cache = new MemoryCache<K, V>();
        cache.setName(name);
        cache.setLoader(loader);
        return cache;
    }

    @Override
    public void destoryCache(String name) {
    }

}
