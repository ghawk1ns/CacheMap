# CacheMap
[![Build Status](https://travis-ci.org/ghawk1ns/CacheMap.svg?branch=master)](https://travis-ci.org/GHawk1ns/CacheMap)



An in-memory cache that loads on a schedule with the benefits of java.util.Map.

Cache config files and other semi-static without having to hardcode and deploy every time the data changes.
```
CacheMap<String, Elephant> cacheMap = CacheMapBuilder.<String, Elephant>newBuilder()
    .ttl(30)
    .ttlTimeUnit(TimeUnit.MINUTES)
    .makeImmutable(true)
    .build(new CacheLoader<String, Elephant>() {
            @Override
            public Map<String, Elephant> load() {
                // grab your map from disk, network, or wherever!
                return elephantClient.fetchTheElephants();
            }

            @Override
            public void onError(Exception e) {
                // bad
            }

            @Override
            public void onLoadComplete() {
                // good
            }
    });
```

example usage
```
// somwhere in your handler
@api("/canFly")
public void canFly(FlyingElephantRequest request) {
    final Elephant elephant = cacheMap.get(request.elephantName());
    if (elephant != null) {
        request.returnSuccess(elephant.canFly());
    } else {
        request.returnNotFound();
    }
}

```
