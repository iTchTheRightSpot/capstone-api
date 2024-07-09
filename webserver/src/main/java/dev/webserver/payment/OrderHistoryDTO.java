package dev.webserver.payment;

import java.io.Serializable;

public record OrderHistoryDTO(
        long date,
        String currency,
        int total,
        String orderNumber,
        PayloadMapper[] detail
) implements Serializable {}