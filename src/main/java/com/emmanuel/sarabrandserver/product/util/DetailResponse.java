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
    @JsonProperty(value = "is_visible")
    private boolean isVisible;
    private String colour;
    private List<String> url;
    private Variant[] variants;

    public DetailResponse(String colour, List<String> url, Variant[] variants) {
        this.colour = colour;
        this.url = url;
        this.variants = variants;
    }

}
