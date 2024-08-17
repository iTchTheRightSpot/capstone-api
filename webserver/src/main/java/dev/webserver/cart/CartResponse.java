package dev.webserver.cart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.webserver.enumeration.CapstoneCurrency;

import java.io.Serializable;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CartResponse(
        String product_id,
        String url,
        String product_name,
        BigDecimal price,
        CapstoneCurrency currency,
        String colour,
        String size,
        String sku,
        int qty,
        double weight,
        @JsonProperty("weight_type")
        String weightType
) implements Serializable { }