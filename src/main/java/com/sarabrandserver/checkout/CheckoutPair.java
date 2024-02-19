package com.sarabrandserver.checkout;

import java.math.BigDecimal;

public record CheckoutPair(double sumOfWeight, BigDecimal total) { }