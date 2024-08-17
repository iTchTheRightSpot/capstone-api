package dev.webserver.category;

import dev.webserver.cache.CacheEnum;
import dev.webserver.cache.CacheImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
class CategoryCacheConfiguration {

    @Bean
    public CacheImpl<CacheEnum, List<CategoryResponse>> allCategories() {
        return new CacheImpl<>(60, 2);
    }

}
