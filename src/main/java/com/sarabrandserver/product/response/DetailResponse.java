package com.sarabrandserver.product.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DetailResponse implements Serializable {

    // product
    private String name;
    private String currency;
    private BigDecimal price;
    private String desc;

    // details
    @JsonProperty(value = "is_visible")
    private boolean isVisible;
    private String colour;
    private List<String> url;
    private Variant[] variants;

}
