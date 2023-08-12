package com.emmanuel.sarabrandserver.product.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DetailResponse {
    private String sku;
    @JsonProperty(value = "is_visible")
    private boolean isVisible;
    private String size;
    private int qty;
    private String colour;
    private List<String> url;
}
