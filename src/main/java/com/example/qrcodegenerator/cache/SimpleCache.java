package com.example.qrcodegenerator.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SimpleCache<K, V>
{
    private final Map<K, V> cache = new HashMap<>();

    public V get(K key)
    {
        return cache.get(key);
    }

    public void put(K key, V value)
    {
        cache.put(key, value);
    }

    public void remove(K key)
    {
        cache.remove(key);
    }
}