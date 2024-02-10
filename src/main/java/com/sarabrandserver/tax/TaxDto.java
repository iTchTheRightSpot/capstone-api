package com.sarabrandserver.tax;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TaxDto(
        @JsonProperty("tax_id")
        @NotNull
        Long id,
        @NotNull
        @NotEmpty
        String name,
        @NotNull
        Double percentage
) {
}