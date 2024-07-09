package dev.webserver.payment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record AddressDTO(
        @NotNull
        @NotEmpty
        String address,

        @NotNull
        @NotEmpty
        String city,

        @NotNull
        @NotEmpty
        String state,

        @NotNull
        String postcode,

        @NotNull
        @NotEmpty
        String country,

        @NotNull
        String deliveryInfo
) implements Serializable {}