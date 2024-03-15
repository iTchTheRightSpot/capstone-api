package dev.capstone.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record CategoryDTO(
        @NotNull(message = "name cannot be null")
        @NotEmpty(message = "name cannot be empty")
        @Size(max = 50, message = "categoryId name cannot exceed length of 50")
        String name,

        @NotNull(message = "visible cannot be null")
        Boolean visible,

        @JsonProperty(value = "parent_id")
        Long parentId
) implements Serializable { }