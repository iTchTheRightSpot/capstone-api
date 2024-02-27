package com.sarabrandserver.payment.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarabrandserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

public record PaymentResponse(
        String reference,
        @JsonProperty("pub_key") String pubKey,
        SarreCurrency currency,
        BigDecimal total
) { }