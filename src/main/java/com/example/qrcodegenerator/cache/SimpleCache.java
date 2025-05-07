package com.example.qrcodegenerator.cache;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SimpleCache {
    private final Map<String, Object> cache = new HashMap<>();

    public void put(String key, Object value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key and value cannot be null");
        }
        synchronized (cache) {
            cache.put(key, value);
        }
    }

    public Optional<Object> get(String key) {
        if (key == null) {
            return Optional.empty();
        }
        synchronized (cache) {
            return Optional.ofNullable(cache.get(key));
        }
    }

    public void remove(String key) {
        if (key != null) {
            synchronized (cache) {
                cache.remove(key);
            }
        }
    }

    public void clear() {
        synchronized (cache) {
            cache.clear();
        }
    }
}