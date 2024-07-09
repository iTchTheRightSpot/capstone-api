package dev.webserver.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.webserver.enumeration.SarreCurrency;

import java.io.Serializable;
import java.math.BigDecimal;

public record CartResponse(
        String product_id,
        String url,
        String product_name,
        BigDecimal price,
        SarreCurrency currency,
        String colour,
        String size,
        String sku,
        int qty,
        double weight,
        @JsonProperty("weight_type")
        String weightType
) implements Serializable { }