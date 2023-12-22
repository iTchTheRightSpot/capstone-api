package com.sarabrandserver.order.response;

import java.math.BigDecimal;

public record PaymentResponse(String pubKey, BigDecimal total) { }