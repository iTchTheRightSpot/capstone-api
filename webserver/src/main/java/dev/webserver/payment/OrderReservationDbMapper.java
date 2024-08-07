package dev.webserver.payment;

public record OrderReservationDbMapper(
        // order reservation
        Long reservationId,
        Integer qty,
        // product sku
        String sku
) {}
