package com.sarabrandserver.product.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record DetailResponse (
        String name,
        String currency,
        BigDecimal price,
        String desc,
        @JsonProperty(value = "is_visible")
        boolean isVisible,
        String colour,
        List<String> urls,
        Variant[] variants
) implements Serializable {

    public DetailResponse (
            String name,
            String currency,
            BigDecimal price,
            String desc,
            String colour,
            List<String> urls,
            Variant[] variants
    ) {
        this(name, currency, price, desc, false, colour, urls, variants);
    }

    public DetailResponse (boolean isVisible, String colour, List<String> urls, Variant[] variants) {
        this("", "", new BigDecimal("0"), "", isVisible, colour, urls, variants);
    }

}