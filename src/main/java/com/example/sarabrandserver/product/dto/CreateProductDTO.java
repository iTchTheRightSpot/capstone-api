package com.example.sarabrandserver.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Builder
@Data
@Getter @Setter
public class CreateProductDTO implements Serializable {

    @JsonProperty(required = true, value = "name")
    @NotNull @NotEmpty
    private String category;

    @JsonProperty(required = true, value = "collection")
    @JsonInclude(NON_EMPTY)
    private String collection;

    @JsonProperty(required = true, value = "name")
    @NotNull @NotEmpty
    @Size(max = 80, message = "Max of 80")
    private String name;

    @JsonProperty(value = "desc")
    @Size(max = 255, message = "Max of 255")
    @NotNull @NotEmpty
    private String desc;

    @JsonProperty(value = "price")
    @NotNull
    private Double price;

    @JsonProperty(value = "currency")
    @NotNull @NotEmpty
    private String currency;

    @JsonProperty(required = true, value = "visible")
    @NotNull
    private Boolean visible;

    @JsonProperty(required = true, value = "qty")
    @NotNull
    private Integer qty;

    @JsonProperty(required = true, value = "size")
    @NotNull @NotEmpty
    private String size;

    @JsonProperty(required = true, value = "colour")
    @NotNull @NotEmpty
    private String colour;

}
