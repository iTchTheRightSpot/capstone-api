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

    @NotNull
    @NotEmpty
    @Size(max = 50, message = "category name cannot exceed length of 50")
    @JsonProperty(value = "name", required = true)
    private String category_name;

    @NotNull(message = "cannot be null") @JsonProperty(value = "visible", required = true)
    private Boolean status;

    @NotNull(message = "cannot be null")
    @JsonProperty(value = "parent")
    private Set<String> sub_category;

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("name", getCategory_name())
                .add("visible", getStatus())
                .add("parent", Json.createArrayBuilder(getSub_category()).build())
                .build();
    }
}
