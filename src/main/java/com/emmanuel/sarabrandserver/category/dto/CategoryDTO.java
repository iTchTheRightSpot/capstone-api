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
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CategoryDTO {

    @NotNull(message = "cannot be null")
    @NotEmpty(message = "cannot be empty")
    @Size(max = 50, message = "category name cannot exceed length of 50")
    @JsonProperty(value = "name", required = true)
    private String name;

    @NotNull(message = "cannot be null")
    @JsonProperty(value = "visible", required = true)
    private Boolean visible;

    @NotNull(message = "cannot be null")
    @JsonProperty(value = "parent")
    private String parent;

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("name", getName())
                .add("visible", getVisible())
                .add("parent", getParent())
                .build();
    }
}
