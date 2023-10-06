package com.sarabrandserver.collection.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record UpdateCollectionDTO(
        @JsonProperty(value = "collection_id")
        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String id,

        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        @Size(max = 50, message = "category name cannot exceed length of 50")
        String name,

        @NotNull(message = "cannot be empty")
        Boolean visible
) implements Serializable { }