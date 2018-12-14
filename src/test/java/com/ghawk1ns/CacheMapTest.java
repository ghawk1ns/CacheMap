package com.ghawk1ns;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class CacheMapTest {

    private static final String KEY = "val";

    @Test
    public void nullLoadTest() throws InterruptedException {
        Semaphore sem = new Semaphore(0);
        final AtomicBoolean initial = new AtomicBoolean(true);
        CacheMap<String, Boolean> cMap = CacheMapBuilder.<String, Boolean>newBuilder()
                .ttl(10000)
                .build(new CacheLoader<String, Boolean>() {
                    @Override
                    public Map<String, Boolean> load() {
                        HashMap<String, Boolean> m = null;
                        // The first time we return a map with a value
                        if (initial.compareAndSet(true, false)) {
                            m = new HashMap<>();
                            m.put(KEY, true);
                        }
                        // The second time load() is called null will be returned
                        return m;
                    }

                    @Override
                    public void onError(Exception e) {
                        Assert.fail(e.getMessage());
                    }

                    @Override
                    public void onLoadComplete() {
                        sem.release();
                    }
                });

        long beforeUpdate = cMap.getLastUpdated();

        sem.acquire();
        Assert.assertTrue(cMap.get(KEY));
        long updated = cMap.getLastUpdated();
        Assert.assertTrue(beforeUpdate < updated);
        // Make sure KEY is the same after we load a null map
        sem.acquire();
        Assert.assertTrue(cMap.get(KEY));
        // Make sure that the old updated time is still the last updated time
        Assert.assertEquals(updated, cMap.getLastUpdated());
    }

    @Test
    public void LoadTest() throws InterruptedException {
        AtomicLong val = new AtomicLong();
        Semaphore sem = new Semaphore(0);
        CacheMap cache = CacheMapBuilder.<String, Long>newBuilder()
                .ttl(1000)
                .build(new CacheLoader<String, Long>() {
                    @Override
                    public Map<String, Long> load() {
                        HashMap<String, Long> m = new HashMap<>();
                        m.put(KEY, val.getAndIncrement());
                        return m;
                    }

                    @Override
                    public void onError(Exception e) {
                        Assert.fail(e.getMessage());
                    }

                    @Override
                    public void onLoadComplete() {
                        sem.release();
                    }
                });

        // Runs until val reaches 5
        for (long expected = 0; expected < 6; expected++) {
            // This will block until onLoadComplete() is called
            sem.acquire();
            Assert.assertEquals(expected, cache.get(KEY));
        }
    }

    @Test
    public void immutableTest() {
        CacheMap m = CacheMapBuilder.newBuilder()
                .ttl(1000)
                .makeImmutable(true)
                .build(EmptyCacheLoader.instance());
        try {
            m.put(KEY, "BAD");
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UnsupportedOperationException);
        }
    }


    @Test
    public void stopAndStartTest() {
        AtomicLong value = new AtomicLong();
        Semaphore sem = new Semaphore(0);
        long ttl = 1000;
        CacheMap m = CacheMapBuilder.<String, Long>newBuilder()
                .initialLoadDelay(100) // removes any doubt of a race condition
                .ttl(ttl)
                .makeImmutable(true)
                .build(new CacheLoader<String, Long>() {
                    @Override
                    public Map<String, Long> load() {
                        HashMap<String, Long> m = new HashMap<>();
                        m.put(KEY, value.get());
                        return m;
                    }

                    @Override
                    public void onError(Exception e) {
                        Assert.fail(e.getMessage());
                    }

                    @Override
                    public void onLoadComplete() {
                        sem.release();
                    }
                });

        try {
            sem.acquire();
            // Cache is loaded
            Assert.assertEquals(value.get(), m.get(KEY));
            m.stop();
            Assert.assertTrue(!m.isActive());
            // Set value to something different and cachemap is never updated
            long expected = value.get();
            long newVal = System.currentTimeMillis();
            value.set(newVal);
            // wait at least ttl
            Thread.sleep(ttl);
            Assert.assertEquals(expected, m.get(KEY));
            // Now lets start it up again and make sure it has changed
            m.restart(true);
            // Assert we have cleared the cache
            Assert.assertTrue(!m.containsKey(KEY));
            Assert.assertTrue(m.isEmpty());
            Assert.assertTrue(m.isActive());
            // wait for the load
            sem.acquire();
            // Assert we have restarted the cache and it has updated with newVal
            Assert.assertEquals(newVal, m.get(KEY));
            long lastVal = System.currentTimeMillis();
            value.set(lastVal);
            // Test one last load
            sem.acquire();
            Assert.assertEquals(lastVal, m.get(KEY));
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }
}
