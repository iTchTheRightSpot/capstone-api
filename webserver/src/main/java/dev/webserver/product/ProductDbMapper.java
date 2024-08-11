package dev.webserver.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.webserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

public record ProductDbMapper(
        // product
        String uuid,
        String name,
        @JsonProperty("image_key")
        String imageKey,
        BigDecimal weight,
        @JsonProperty("weight_type")
        String weightType,
        String description,
        // price currency
        BigDecimal price,
        SarreCurrency currency,
        // category
        @JsonProperty("category_name")
        String categoryName
) {}
