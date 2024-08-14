package dev.webserver.product;

import dev.webserver.cache.CacheImpl;
import dev.webserver.util.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class ProductCacheListener implements IProductCachePublisher {

    private final CacheImpl<String, Pageable<ProductResponse>> productResponsePageableCache;
    private final CacheImpl<String, List<DetailResponse>> listOfDetailResponseCache;

    @Async
    @Override
    public void evictAll() {
        productResponsePageableCache.evictAll();
        listOfDetailResponseCache.evictAll();
    }

    @Override
    public Optional<Pageable<ProductResponse>> pageOfProductResponse(final String key) {
        return productResponsePageableCache.getIfPresent(key);
    }

    @Async
    @Override
    public void addPageOfProductResponseToCache(final String key, final Pageable<ProductResponse> pageable) {
        productResponsePageableCache.put(key, pageable);
    }

    @Override
    public Optional<List<DetailResponse>> listOfDetailResponse(final String key) {
        return listOfDetailResponseCache.getIfPresent(key);
    }

    @Async
    @Override
    public void addListOfDetailResponseToCache(final String key, final List<DetailResponse> pageable) {
        listOfDetailResponseCache.put(key, pageable);
    }

}
