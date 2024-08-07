package dev.webserver.payment;

import dev.webserver.enumeration.SarreCurrency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDetailDbMapper(
        // payment detail
        LocalDateTime createdAt,
        SarreCurrency currency,
        BigDecimal amount,
        String referenceId,
        // PayloadMapper
        String detail
) {}
