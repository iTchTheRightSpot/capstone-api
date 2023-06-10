package com.example.sarabrandserver.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.List;

public record CategoryDTO(
        @NotNull @NotEmpty
        @JsonProperty(value = "category_name", required = true)
        String category_name,
        @NotNull @NotEmpty
        @JsonProperty(value = "sub_category", required = true)
        List<String> sub_category
) {

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("category_name", category_name())
                .add("sub_category", (JsonValue) sub_category())
                .build();
    }
}
