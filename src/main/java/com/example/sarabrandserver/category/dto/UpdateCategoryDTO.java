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

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UpdateCategoryDTO {

    @NotNull
    @NotEmpty
    @Size(max = 50, message = "category name cannot exceed length of 50")
    @JsonProperty(value = "old_name", required = true)
    private String old_name;

    @NotNull
    @NotEmpty
    @Size(max = 50, message = "category name cannot exceed length of 50")
    @JsonProperty(value = "new_name", required = true)
    private String new_name;

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("old_name", getOld_name())
                .add("new_name", getNew_name())
                .build();
    }

}
