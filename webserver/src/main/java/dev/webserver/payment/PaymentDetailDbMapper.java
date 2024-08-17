package dev.webserver.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentDetailDbMapper(
        // order reservation
        @JsonProperty("reservation_id")
        Long reservationId,
        Integer qty,
        // product sku
        @JsonProperty("sku_id")
        Long skuId
) {}
