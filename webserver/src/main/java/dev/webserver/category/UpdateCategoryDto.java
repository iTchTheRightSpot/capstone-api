package dev.webserver.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record UpdateCategoryDto(
        @NotNull(message = "category category_id cannot be null")
        @JsonProperty(value = "category_id")
        Long categoryId,
        @JsonProperty(value = "parent_id")
        Long parentId,
        @NotNull(message = "category name cannot be null")
        @NotEmpty(message = "category name cannot be empty")
        @Size(max = 50, message = "category name cannot exceed length of 50")
        String name,
        @NotNull(message = "category visibility cannot be null")
        Boolean visible
) implements Serializable {}