package dev.webserver.product;

import dev.webserver.util.Pageable;

import java.util.List;
import java.util.Optional;

public interface IProductCachePublisher {

    void evictAll();

    Optional<Pageable<ProductResponse>> pageOfProductResponse(final String key);
    void addPageOfProductResponseToCache(final String key, Pageable<ProductResponse> pageable);

    Optional<List<DetailResponse>> listOfDetailResponse(final String key);
    void addListOfDetailResponseToCache(final String key, final List<DetailResponse> pageable);
}
