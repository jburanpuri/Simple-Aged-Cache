package io.collective;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class SimpleAgedCache {
    private final Clock clock;
    private final Map<Object, ExpirableEntry> cache;

    public SimpleAgedCache(Clock clock) {
        this.clock = clock;
        this.cache = new ConcurrentHashMap<>();
    }

    public SimpleAgedCache() {
        this(Clock.systemDefaultZone());
    }

    public void put(Object key, Object value, int retentionInMillis) {
        long expiryTime = clock.millis() + retentionInMillis;
        cache.put(key, new ExpirableEntry(value, expiryTime));
    }

    public boolean isEmpty() {
        cleanUp();
        return cache.isEmpty();
    }

    public int size() {
        cleanUp();
        return cache.size();
    }

    public Object get(Object key) {
        ExpirableEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.value;
        }
        return null;
    }

    private void cleanUp() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private class ExpirableEntry {
        final Object value;
        private final long expiryTime;

        ExpirableEntry(Object value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }

        boolean isExpired() {
            return clock.millis() > expiryTime;
        }
    }
}
