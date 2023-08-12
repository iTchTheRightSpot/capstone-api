package com.emmanuel.sarabrandserver.product.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@Data
public class CreateProductDTO implements Serializable {

    @JsonProperty(required = true, value = "category")
    @NotNull(message = "Please select category as product has to below to a category")
    @NotEmpty(message = "Please select category as product has to below to a category")
    private String category;

    @JsonProperty(required = true, value = "collection")
    @NotNull(message = "Product collection cannot be null")
    private String collection;

    @JsonProperty(required = true, value = "name") // product_name
    @NotNull(message = "Name cannot be null")
    @NotEmpty(message = "Please enter product name")
    @Size(max = 80, message = "Max of 80")
    private String name;

    @JsonProperty(value = "desc")
    @Size(max = 255, message = "Max of 255")
    @NotNull(message = "Please enter product description")
    @NotEmpty(message = "Please enter product description")
    private String desc;

    @JsonProperty(value = "price")
    @NotNull(message = "Please enter product price")
    private BigDecimal price;

    @JsonProperty(value = "currency")
    @NotNull(message = "Please enter or choose a product currency")
    @NotEmpty(message = "Please enter or choose a product currency")
    private String currency;

    @JsonProperty(required = true, value = "visible")
    @NotNull(message = "Please choose if product should be visible")
    private Boolean visible;

    @JsonProperty(required = true, value = "qty")
    @NotNull(message = "Please enter or choose product quantity")
    private Integer qty;

    @JsonProperty(required = true, value = "size")
    @NotNull(message = "Please enter or choose product size")
    @NotEmpty(message = "Please enter or choose product size")
    private String size;

    @JsonProperty(required = true, value = "colour")
    @NotNull(message = "Please enter or choose product colour")
    @NotEmpty(message = "Please enter or choose product colour")
    private String colour;

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("category", getCategory())
                .add("collection", getCollection())
                .add("name", getName())
                .add("desc", getDesc())
                .add("price", getPrice())
                .add("currency", getCurrency())
                .add("visible", getVisible())
                .add("qty", getQty())
                .add("size", getSize())
                .add("colour", getColour())
                .build();
    }

}
