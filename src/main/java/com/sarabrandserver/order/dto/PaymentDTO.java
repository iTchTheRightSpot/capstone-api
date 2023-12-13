package com.sarabrandserver.order.dto;

import com.sarabrandserver.address.AddressDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record PaymentDTO(
        @NotNull
        @NotEmpty
        String firstname,

        @NotNull
        @NotEmpty
        String lastname,

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
        BigDecimal total,

        @NotNull
        @NotEmpty
        String paymentProvider,

        @NotNull
        SkuQtyDTO[] dto,

        @NotNull
        AddressDTO address
) implements Serializable {}