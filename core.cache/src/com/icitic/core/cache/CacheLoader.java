package com.icitic.core.cache;

public interface CacheLoader<K, V> {

    V load(K key);

}
