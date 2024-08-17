package dev.webserver.payment;

import java.math.BigDecimal;

public record CartTotalDbMapper(
        // cart
        Integer qty,
        // price currency
        BigDecimal price,
        // product
        Double weight
) {}
