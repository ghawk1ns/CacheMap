package com.ghawk1ns;

import java.util.Collections;
import java.util.Map;

public class EmptyCacheLoader implements CacheLoader {

    private static final EmptyCacheLoader singleton = new EmptyCacheLoader();

    private EmptyCacheLoader() { }

    public static EmptyCacheLoader instance() {
        return singleton;
    }

    @Override
    public Map load() {
        return Collections.emptyMap();
    }

    @Override
    public void onError(Exception e) {
        // noop
    }

    @Override
    public void onLoadComplete() {
        // noop
    }
}
