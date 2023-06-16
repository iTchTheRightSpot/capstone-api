package com.example.sarabrandserver.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.Set;


@Builder
public record CategoryDTO(
        @NotNull @NotEmpty @Size(max = 50, message = "category name cannot exceed length of 50")
        @JsonProperty(value = "category_name", required = true)
        String category_name,
        @NotNull
        Boolean status,
        @NotNull @NotEmpty
        @JsonProperty(value = "sub_category", required = true)
        Set<String> sub_category
) {
    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("category_name", category_name())
                .add("status", status)
                .add("sub_category", Json.createArrayBuilder(sub_category()).build())
                .build();
    }
}
