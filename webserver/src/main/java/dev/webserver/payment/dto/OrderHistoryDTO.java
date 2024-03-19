package dev.webserver.payment.dto;

import java.io.Serializable;

public record OrderHistoryDTO(
        long date,
        String currency,
        int total,
        String orderNumber,
        PayloadMapper[] detail
) implements Serializable {}