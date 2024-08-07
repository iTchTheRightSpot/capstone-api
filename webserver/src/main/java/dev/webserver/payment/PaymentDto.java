package dev.webserver.payment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record PaymentDto(
        @NotNull
        @NotEmpty
        String email,
        @NotNull
        @NotEmpty
        String name,
        @NotNull
        @NotEmpty
        String phone,
        @NotNull
        @NotEmpty
        String currency,
        @NotNull
        BigDecimal total,
        @NotNull
        @NotEmpty
        String paymentProvider,
        @NotNull
        SkuQtyDto[] dto,
        @NotNull
        AddressDto address
) implements Serializable {}