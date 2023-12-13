package com.sarabrandserver.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record PaymentDTO(
        @NotNull
        @NotEmpty
        String name,

        @NotNull
        @NotEmpty
        String email,

        @NotNull
        @NotEmpty
        String phoneNumber,

        @NotNull
        @NotEmpty
        String currency,

        @NotNull
        @NotEmpty
        String paymentProvider,

        @NotNull
        @NotEmpty
        String skus,

        @NotNull
        Integer qty
) implements Serializable {}