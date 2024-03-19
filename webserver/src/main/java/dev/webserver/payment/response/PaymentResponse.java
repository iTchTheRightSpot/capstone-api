package dev.webserver.payment.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.webserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

public record PaymentResponse(
        String reference,
        @JsonProperty("pub_key") String pubKey,
        SarreCurrency currency,
        BigDecimal total
) { }