package com.ghawk1ns;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CacheMap<K, V> extends ForwardingMap<K, V> {

    /**
     * @see CacheMapBuilder#makeImmutable(boolean)
     */
    private final boolean makeImmutable;

    /**
     * @see CacheMapBuilder#initialLoadDelay(long)
     */
    private final long initialLoadDelay;

    /**
     * @see CacheMapBuilder#ttl(long)
     */
    private final long ttl;

    /**
     * @see CacheMapBuilder#ttlTimeUnit(TimeUnit)
     */
    private final TimeUnit ttlTimeUnit;

    /**
     * @see CacheMapBuilder#scheduledExecutorService(ScheduledExecutorService)
     */
    private final ScheduledExecutorService scheduledExecutorService;

    /**
     * @see CacheMapBuilder#loader(CacheLoader)
     */
    private final CacheLoader<K, V> loader;

    private ScheduledFuture<?> scheduledFuture;
    private Map<K, V> cache; // underlying cache
    private long lastUpdated; // time since last load occurred

    /**
     * @see CacheMapBuilder#build()
     */
    CacheMap(boolean makeImmutable, long initialLoadDelay, long ttl, TimeUnit ttlTimeUnit,
             ScheduledExecutorService scheduledExecutorService, CacheLoader<K, V> loader) {
        if (loader == null) {
            throw new IllegalArgumentException("A CacheLoader must be provided");
        } else if (ttl <= 0) {
            throw new IllegalArgumentException("ttl must be non-negative");
        }
        this.ttl = ttl;
        this.ttlTimeUnit = ttlTimeUnit;
        this.initialLoadDelay = initialLoadDelay;
        this.makeImmutable = makeImmutable;
        this.loader = loader;
        this.scheduledExecutorService = scheduledExecutorService == null ?
                Executors.newSingleThreadScheduledExecutor() : scheduledExecutorService;
        // Do this last
        scheduledFuture = init(true);
    }

    private ScheduledFuture<?> init(boolean newCache) {
        if (newCache) {
            cache = makeImmutable ? ImmutableMap.of() : new HashMap<>();
        }
        return this.scheduledExecutorService.scheduleWithFixedDelay(this::_load, initialLoadDelay, ttl, ttlTimeUnit);
    }

    private void _load() {
        Map<K, V> freshMap = loader.load();
        if (freshMap != null) {
            Map<K, V> old = cache;
            try {
                if (makeImmutable) {
                    cache = ImmutableMap.copyOf(freshMap);
                } else {
                    Map<K, V> tmp = new HashMap<>(freshMap.size());
                    // copies everything into a new map
                    freshMap.forEach((k,v) -> tmp.merge(k, v, (unused, freshVal) -> freshVal));
                    // atomic transaction so we can pull from the cache while this is happening
                    cache = tmp;
                }
                lastUpdated = System.currentTimeMillis();
            } catch (Exception e) {
                // fallback to the previous cache
                cache = old;
                // pass any exceptions along
                loader.onError(e);
                return;
            }
        }
        loader.onLoadComplete();
    }

    /**
     * Stops {@link CacheMap#scheduledFuture} from loading the map in the future
     * @return true if cancelled, false if it couldn't, typically because it was already cancelled
     */
    public boolean stop() {
        return scheduledFuture.cancel(true);
    }

    /**
     * Restarts the {@link CacheMap#ttl} on the cache
     * @param dropExistingImmediately drops items in {@link CacheMap#cache} immediately rather than
     *                                waiting for {@link CacheMap#loader} to replace it
     */
    public void restart(boolean dropExistingImmediately) {
        stop();
        scheduledFuture = init(dropExistingImmediately);
    }

    /**
     *
     * @return true if the cache is scheduled to be updated
     */
    public boolean isActive() {
        return !scheduledFuture.isCancelled() && !scheduledFuture.isDone();
    }

    /**
     *
     * @return remaining time in {@link CacheMap#ttlTimeUnit} until the next load
     */
    public long remainingTTL() {
        return scheduledFuture.getDelay(ttlTimeUnit);
    }

    /**
     *
     * @return the time since the last load occurred
     */
    public long getLastUpdated() {
        return lastUpdated;
    }

    @Override
    protected Map<K, V> delegate() {
        return cache;
    }
}