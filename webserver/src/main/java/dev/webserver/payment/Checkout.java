package dev.webserver.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record Checkout (
        String principal,
        @JsonProperty("weight_detail")
        String weightDetail,
        @JsonProperty("ship_cost")
        BigDecimal ship,
        @JsonProperty("tax_name")
        String tax,
        @JsonProperty("tax_rate")
        double rate,
        @JsonProperty("tax_total")
        BigDecimal taxTotal,
        @JsonProperty("sub_total")
        BigDecimal subTotal,
        BigDecimal total
) { }