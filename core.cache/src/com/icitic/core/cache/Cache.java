package com.icitic.core.cache;

import java.util.Collection;

import com.google.common.base.Function;
import com.icitic.core.model.object.INameObject;

public interface Cache<K, V> extends INameObject, Function<K, V> {

    void put(K key, V value);

    V get(K key);

    void remove(K key);

    void removeAll();
    
    void removeAll(Collection<K> keys);
}
