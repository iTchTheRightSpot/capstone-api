package com.sarabrandserver.collection.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CollectionDTO(
        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String name,
        @NotNull(message = "cannot be empty")
        Boolean visible
) { }