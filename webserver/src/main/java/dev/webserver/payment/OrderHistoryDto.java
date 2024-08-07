package dev.webserver.payment;

import dev.webserver.enumeration.SarreCurrency;

import java.io.Serializable;
import java.math.BigDecimal;

public record OrderHistoryDto(
        long date,
        SarreCurrency currency,
        BigDecimal amount,
        String orderNumber,
        OrderHistoryDbMapper[] detail
) implements Serializable {}