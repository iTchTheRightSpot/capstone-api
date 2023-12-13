package com.sarabrandserver.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record SkuQtyDTO(
        @NotNull
        @NotEmpty
        String sku,

        @NotNull
        Integer qty
) implements Serializable {}