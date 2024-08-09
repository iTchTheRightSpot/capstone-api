package dev.webserver.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponse (
        @JsonProperty(value = "product_id")
        String id,
        String name,
        String desc,
        BigDecimal price,
        String currency,
        @JsonProperty("image_key")
        String imageKey,
        String category,
        double weight,
        @JsonProperty("weight_type")
        String weightType
) implements Serializable {}