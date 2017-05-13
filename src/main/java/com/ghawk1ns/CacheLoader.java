package com.ghawk1ns;

import java.util.Map;

public interface CacheLoader<K, V> {

    /**
     *
     * @return a {@link java.util.Map} to be cached or null if the cache shouldn't be re-loaded
     */
    public Map<K, V> load();

    /**
     *
     * Returns an exception if one occurred while loading the map, it almost certainly won't ever happen.
     */
    public void onError(Exception e);

    /**
     * Called when {@link CacheMap} successfully loads a map or null returned by {@link CacheLoader#load()}
     */
    public void onLoadComplete();
}
