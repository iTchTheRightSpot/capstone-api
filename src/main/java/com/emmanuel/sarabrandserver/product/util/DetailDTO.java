package com.emmanuel.sarabrandserver.product.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.json.Json;
import javax.json.JsonObject;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class DetailDTO {

    @JsonProperty(value = "sku")
    @NotNull @NotEmpty
    private String sku;

    @JsonProperty(value = "is_visible")
    @NotNull
    private Boolean visible;

    // Properties of many-to-1 and 1-to-many
    @JsonProperty(required = true, value = "qty")
    @NotNull @NotEmpty
    private Integer qty;

    @JsonProperty(required = true, value = "size")
    @NotNull @NotEmpty
    private String size;

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("sku", getSku())
                .add("visible", getVisible())
                .add("qty", getQty())
                .add("size", getSize())
                .build();
    }

}
