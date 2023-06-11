package com.example.sarabrandserver.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import javax.json.Json;
import javax.json.JsonObject;

public record UpdateCategoryDTO (
        @NotNull @NotEmpty @Size(max = 50, message = "category name cannot exceed length of 50")
        @JsonProperty(value = "old_name", required = true)
        String old_name,
        @NotNull @NotEmpty @Size(max = 50, message = "category name cannot exceed length of 50")
        @JsonProperty(value = "new_name", required = true)
        String new_name
) {
    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("old_name", old_name())
                .add("new_name", new_name())
                .build();
    }
}
