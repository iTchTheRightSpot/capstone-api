package dev.webserver.payment;

public record PaymentDetailDbMapper(
        // order reservation
        Long reservationId,
        Integer qty,
        // product sku
        Long skuId
) {}
