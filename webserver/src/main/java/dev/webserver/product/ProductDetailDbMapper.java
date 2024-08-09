package dev.webserver.product;

public record ProductDetailDbMapper(
        //product detail
        String colour,
        Boolean isVisible,
        // product image
        String imageKey,
        // variant
        String variants
) {}
