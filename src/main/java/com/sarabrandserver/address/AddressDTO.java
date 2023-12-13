package com.sarabrandserver.address;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record AddressDTO(
        @NotNull
        String unitNumber,

        @NotNull
        @NotEmpty
        String streetNumber,

        @NotNull
        @NotEmpty
        String address1,

        @NotNull
        String address2,

        @NotNull
        @NotEmpty
        String city,

        @NotNull
        @NotEmpty
        String stateOrProvince,

        @NotNull
        String postalZipCode,

        @NotNull
        @NotEmpty
        String country
) implements Serializable {}