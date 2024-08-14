package dev.webserver.product;

import dev.webserver.cache.CacheImpl;
import dev.webserver.util.Pageable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
class ProductCacheConfiguration {

    /**
     * Cache for endpoints that return a {@link Pageable} of {@link ProductResponse}.
     */
    @Bean
    public CacheImpl<String, Pageable<ProductResponse>> productResponsePageableCache() {
        return new CacheImpl<>(60, 100);
    }

    /**
     * Cache for endpoints that return a {@link List} of {@link DetailResponse}.
     */
    @Bean
    public CacheImpl<String, List<DetailResponse>> listOfDetailResponseCache() {
        return new CacheImpl<>(60, 100);
    }

}
