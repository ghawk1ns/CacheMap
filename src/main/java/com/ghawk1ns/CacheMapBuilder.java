package com.ghawk1ns;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CacheMapBuilder<K, V> {

    /**
     * @see CacheMapBuilder#makeImmutable(boolean)
     */
    private boolean makeImmutable;

    /**
     * @see CacheMapBuilder#initialLoadDelay(long)
     */
    private long initialLoadDelay;

    /**
     * @see CacheMapBuilder#ttl(long)
     */
    private long ttl;

    /**
     * @see CacheMapBuilder#ttlTimeUnit(TimeUnit)
     */
    private TimeUnit ttlTimeUnit = TimeUnit.MILLISECONDS;

    /**
     * @see CacheMapBuilder#scheduledExecutorService(ScheduledExecutorService)
     */
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * @see CacheMapBuilder#loader(CacheLoader)
     */
    private CacheLoader<K, V> loader;

    /**
     *
     * @param makeImmutable true if {@link CacheMap} should immutable when not loading
     * @see com.google.common.collect.ImmutableMap
     */
    public CacheMapBuilder<K, V> makeImmutable(boolean makeImmutable) {
        this.makeImmutable = makeImmutable;
        return this;
    }

    /**
     *
     * @param initialLoadDelay the time in {@link CacheMapBuilder#ttlTimeUnit} units before the first load can occur
     */
    public CacheMapBuilder<K, V> initialLoadDelay(long initialLoadDelay) {
        this.initialLoadDelay = initialLoadDelay;
        return this;
    }

    /**
     *
     * @param ttl the time to live lifespan of the cache, {@link CacheLoader#load()} is called at the end of every ttl
     */
    public CacheMapBuilder<K, V> ttl(long ttl) {
        this.ttl = ttl;
        return this;
    }

    /**
     *
     * @param ttlTimeUnit the time unit ttl is observed as. Defaults to {@link TimeUnit#MILLISECONDS}
     */
    public CacheMapBuilder<K, V> ttlTimeUnit(TimeUnit ttlTimeUnit) {
        this.ttlTimeUnit = ttlTimeUnit;
        return this;
    }

    /**
     *
     * @param scheduledExecutorService A scheduledExecutorService can be provided you if you already maintain one,
     * otherwise a default is provided:
     * @see java.util.concurrent.Executors#newSingleThreadScheduledExecutor()
     */
    public CacheMapBuilder<K, V> scheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
        return this;
    }

    /**
     * Note: Failure to set will result in an {@link IllegalArgumentException}
     * @see CacheLoader
     */
    public CacheMapBuilder<K, V> loader(CacheLoader<K, V> loader) {
        this.loader = loader;
        return this;
    }

    /**
     * @see CacheMapBuilder#loader(CacheLoader)
     * @see CacheMapBuilder#build()
     */
    public CacheMap<K, V> build(CacheLoader<K, V> loader) {
        return new CacheMap<>(makeImmutable, initialLoadDelay, ttl, ttlTimeUnit, scheduledExecutorService, loader);
    }

    /**
     *
     * @return A {@link CacheMap}
     * @throws IllegalArgumentException if {@link CacheMapBuilder#loader} isn't set OR {@link CacheMapBuilder#ttl} <= 0
     */
    public CacheMap<K, V> build() {
        return new CacheMap<>(makeImmutable, initialLoadDelay, ttl, ttlTimeUnit, scheduledExecutorService, loader);
    }

    // convenience method
    public static <K, V> CacheMapBuilder<K, V> newBuilder() {
        return new CacheMapBuilder<K, V>();
    }
}