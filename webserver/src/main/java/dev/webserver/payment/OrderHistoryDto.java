package dev.webserver.payment;

import dev.webserver.enumeration.CapstoneCurrency;

import java.io.Serializable;
import java.math.BigDecimal;

public record OrderHistoryDto(
        long date,
        CapstoneCurrency currency,
        BigDecimal amount,
        String orderNumber,
        OrderHistoryDbMapper[] detail
) implements Serializable {}