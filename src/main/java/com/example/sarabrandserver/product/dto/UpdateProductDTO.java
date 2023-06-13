package com.example.sarabrandserver.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UpdateProductDTO {

    @JsonProperty(required = true, value = "new_name")
    @NotNull @NotEmpty
    private String oldName;

    @JsonProperty(value = "old_name")
    private String newName;

    @JsonProperty(value = "qty")
    private Integer qty;

    @JsonProperty(value = "size")
    private String size;

    @JsonProperty(required = true, value = "colour")
    private String colour;

}
