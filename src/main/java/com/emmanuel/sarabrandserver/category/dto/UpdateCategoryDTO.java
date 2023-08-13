package com.emmanuel.sarabrandserver.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.json.Json;
import javax.json.JsonObject;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UpdateCategoryDTO {

    @NotNull(message = "cannot be empty")
    @JsonProperty(required = true)
    private String id;

    @NotNull(message = "cannot be empty")
    @NotEmpty(message = "cannot be empty")
    @Size(max = 50, message = "category name cannot exceed length of 50")
    private String name;

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("id", getId())
                .add("name", getName())
                .build();
    }

}
