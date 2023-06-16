package com.example.sarabrandserver.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UpdateProductDTO implements Serializable {

    @JsonProperty(required = true, value = "old_name")
    @NotNull @NotEmpty
    private String oldName;

    @JsonProperty(value = "new_name")
    @JsonInclude(value = NON_EMPTY)
    private String newName;

    @JsonProperty(value = "sku")
    @NotNull @NotEmpty
    private String sku;

    @JsonProperty(value = "description")
    @Size(max = 255, message = "Max of 255")
    @NotNull @NotEmpty
    private String desc;

    @JsonProperty(required = true, value = "qty")
    @NotNull @NotEmpty
    private Integer qty;

    @JsonProperty(required = true, value = "size")
    @NotNull @NotEmpty
    private String size;

    @JsonProperty(required = true, value = "colour")
    @NotNull @NotEmpty
    private String colour;

    @JsonProperty(required = true, value = "price")
    @NotNull @NotEmpty
    private Double price;

}
