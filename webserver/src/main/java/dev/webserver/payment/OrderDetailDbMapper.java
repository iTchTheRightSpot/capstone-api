package dev.webserver.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.webserver.enumeration.CapstoneCurrency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDetailDbMapper(
        // payment detail
        @JsonProperty("created_at")
        LocalDateTime createdAt,
        CapstoneCurrency currency,
        BigDecimal amount,
        @JsonProperty("reference_id")
        String referenceId,
        // PayloadMapper
        String detail
) {}
