package dev.webserver.checkout;

import java.io.Serializable;
import java.math.BigDecimal;

public record CheckoutPair(double sumOfWeight, BigDecimal total) implements Serializable { }