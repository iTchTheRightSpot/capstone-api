package dev.webserver.payment;

import java.io.Serializable;
import java.math.BigDecimal;

public record CheckoutPair(double sumOfWeight, BigDecimal total) implements Serializable { }