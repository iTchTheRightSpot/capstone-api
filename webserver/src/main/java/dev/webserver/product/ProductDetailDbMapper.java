package dev.webserver.product;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductDetailDbMapper(
        //product detail
        String colour,
        @JsonProperty("is_visible")
        Boolean isVisible,
        // product image
        @JsonProperty("image_key")
        String imageKey,
        // variant
        String variants
) {}
