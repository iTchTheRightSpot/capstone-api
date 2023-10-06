package com.sarabrandserver.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

public record UpdateProductDTO(
        @JsonProperty(value = "product_id")
        @NotNull(message = "cannot be empty")
        String uuid,

        @JsonProperty(value = "name")
        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String name,

        @Size(max = 400, message = "Max of 255")
        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String desc,

        @NotNull(message = "cannot be empty")
        BigDecimal price,

        @NotNull(message = "cannot be empty")
        String category,

        @JsonProperty(value = "category_id")
        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String categoryId,

        @NotNull(message = "cannot be empty")
        String collection,

        @JsonProperty(value = "collection_id")
        @NotNull(message = "cannot be empty")
        String collectionId
) implements Serializable { }