package com.emmanuel.sarabrandserver.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProductDTO implements Serializable {

    @JsonProperty(value = "product_id")
    @NotNull
    private Long id;

    @JsonProperty(value = "name")
    @NotNull @NotEmpty
    private String name;

    @Size(max = 255, message = "Max of 255")
    @NotNull @NotEmpty
    private String desc;

    @NotNull @NotEmpty
    private Double price;

}
