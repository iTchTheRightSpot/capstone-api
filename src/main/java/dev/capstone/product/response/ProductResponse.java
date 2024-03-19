package dev.capstone.product.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ProductResponse (
        @JsonProperty(value = "product_id")
        String id,
        String name,
        String desc,
        BigDecimal price,
        String currency,
        @JsonProperty("image")
        String imageUrl,
        String category,
        double weight,
        @JsonProperty("weight_type")
        String weightType
) {

    public ProductResponse(
            String id,
            String name,
            String desc,
            BigDecimal price,
            String currency,
            String imageUrl
    ) {
        this(id, name, desc, price, currency, imageUrl, "", 0, "");
    }

    public ProductResponse(
            String id,
            String name,
            String desc,
            BigDecimal price,
            String currency,
            String imageUrl,
            String category
    ) {
        this(id, name, desc, price, currency, imageUrl, category, 0, "");
    }

    public ProductResponse(
            String id,
            String name,
            BigDecimal price,
            String currency,
            String imageUrl
    ) {
        this(id, name, "", price, currency, imageUrl, "", 0, "");
    }

    public ProductResponse(
            String id,
            String name,
            BigDecimal price,
            String currency,
            String imageUrl,
            String category
    ) {
        this(id, name, "", price, currency, imageUrl, category, 0, "");
    }

    public ProductResponse (
            @JsonProperty(value = "product_id")
            String id,
            String name,
            String desc,
            BigDecimal price,
            String currency,
            @JsonProperty("image")
            String imageUrl,
            double weight,
            @JsonProperty("weight_type")
            String weightType

    ) {
        this(id, name, desc, price, currency, imageUrl, "", weight, weightType);
    }

}