package com.ghawk1ns;

import org.junit.Assert;
import org.junit.Test;

public class CacheMapBuilderTest {

    @Test
    public void invalidTTLTest() {
        // We expect to throw an exception for non-positive numbers
        for (int tll = -10; tll <= 10; tll++) {
            try {
                CacheMapBuilder.newBuilder()
                        .ttl(tll)
                        .build(EmptyCacheLoader.instance());
                // Assert no exception is thrown for values > 0
                Assert.assertTrue(tll > 0);
            } catch (IllegalArgumentException E) {
                // Assert an exception is thrown for all values less than 1
                Assert.assertTrue(tll <= 0);
            }
        }
    }

    @Test
    public void noLoaderTest() {
        try {
            CacheMapBuilder.newBuilder()
                    .ttl(1)
                    .build();
            Assert.fail("No Loader provided but builder was successful");
        } catch (IllegalArgumentException E) {
            // pass
        }

        try {
            CacheMapBuilder.newBuilder()
                    .ttl(1)
                    .build(EmptyCacheLoader.instance());
        } catch (IllegalArgumentException E) {
            Assert.fail("Loader was provided but illegal argument was thrown");
        }
    }
}
