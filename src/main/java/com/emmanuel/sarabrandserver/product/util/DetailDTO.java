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

    @JsonProperty(required = true)
    @NotNull(message = "cannot be empty")
    @NotEmpty(message = "cannot be empty")
    private String sku;

    @JsonProperty(value = "is_visible", required = true)
    @NotNull(message = "cannot be empty")
    private Boolean isVisible;

    @JsonProperty(required = true)
    @NotNull(message = "cannot be empty")
    private Integer qty;

    @JsonProperty(required = true)
    @NotNull(message = "cannot be empty")
    @NotEmpty(message = "cannot be empty")
    private String size;

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("sku", getSku())
                .add("is_visible", getIsVisible())
                .add("qty", getQty())
                .add("size", getSize())
                .build();
    }

}
