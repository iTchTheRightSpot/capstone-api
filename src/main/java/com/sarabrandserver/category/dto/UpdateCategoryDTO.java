package com.sarabrandserver.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCategoryDTO(
        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot not empty")
        @JsonProperty(value = "category_id")
        String id,

        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        @Size(max = 50, message = "category name cannot exceed length of 50")
        String name,

        @NotNull(message = "cannot be empty")
        Boolean visible
) { }