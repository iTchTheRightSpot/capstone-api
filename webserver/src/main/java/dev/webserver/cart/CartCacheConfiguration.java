package dev.webserver.cart;

import dev.webserver.cache.CacheImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class CartCacheConfiguration {

    @Bean
    public CacheImpl<String, List<CartResponse>> listOfCartResponseCache() {
        return new CacheImpl<>(10, 30);
    }

}
