package com.ghawk1ns;

import java.util.Map;

public interface CacheLoader<K, V> {

    /**
     *
     * @return a {@link java.util.Map} to be cached
     * NullPointerException is returned in {@link CacheLoader#onError(Exception)} if load returns a null
     */
    public Map<K, V> load();

    /**
     *
     * Returns an exception if one occurred while loading the map or if {@link CacheLoader#load()} returns null
     */
    public void onError(Exception e);

    /**
     * Called when {@link CacheMap} successfully loads a map returned by {@link CacheLoader#load()}
     */
    public void onLoadComplete();
}
