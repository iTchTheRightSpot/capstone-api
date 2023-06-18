package com.example.sarabrandserver.category.dto;

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
    @JsonProperty(value = "category_name", required = true)
    private String category_name;

    @NotNull
    private Boolean status;

    @NotNull
    @NotEmpty
    @JsonProperty(value = "sub_category", required = true)
    private Set<String> sub_category;

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("category_name", getCategory_name())
                .add("status", getStatus())
                .add("sub_category", Json.createArrayBuilder(getSub_category()).build())
                .build();
    }
}
