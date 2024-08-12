package dev.webserver.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.webserver.enumeration.CapstoneCurrency;

import java.io.Serializable;
import java.math.BigDecimal;

public record PaymentResponse(
        String reference,
        @JsonProperty("pub_key") String pubKey,
        CapstoneCurrency currency,
        BigDecimal total
) implements Serializable { }