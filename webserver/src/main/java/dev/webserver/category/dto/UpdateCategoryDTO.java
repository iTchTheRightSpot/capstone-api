package dev.webserver.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record UpdateCategoryDTO(
        @NotNull(message = "cannot be empty")
        @JsonProperty(value = "category_id")
        Long id,

        @JsonProperty(value = "parent_id")
        Long parentId,

        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        @Size(max = 50, message = "name cannot exceed length of 50")
        String name,

        @NotNull(message = "cannot be null")
        Boolean visible
) implements Serializable { }