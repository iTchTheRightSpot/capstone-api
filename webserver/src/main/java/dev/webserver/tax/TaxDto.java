package dev.webserver.tax;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record TaxDto(
        @JsonProperty("tax_id")
        @NotNull(message = "tax tax_id cannot be null")
        Long id,
        @NotNull(message = "tax name cannot be null")
        @NotEmpty(message = "tax name cannot be empty")
        String name,
        @NotNull(message = "tax rate cannot be null")
        BigDecimal rate
) implements Serializable {}