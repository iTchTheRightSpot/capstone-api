package com.emmanuel.sarabrandserver.product.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@NoArgsConstructor @Getter @Setter
public class DetailResponse {
    @JsonIgnore
    @Value(value = "${s3.pre-assigned.url}")
    private String ASSIGNED_URL;

    private String sku;
    @JsonProperty(value = "is_visible")
    private boolean isVisible;
    private String size;
    private int qty;
    private String colour;
    private List<String> url;

    public DetailResponse(String sku, boolean isVisible, String size, int qty, String colour, List<String> key) {
        this.sku = sku;
        this.isVisible = isVisible;
        this.size = size;
        this.qty = qty;
        this.colour = colour;
        this.url = key.stream() // Append image key to pre-assigned url
                .map(str -> ASSIGNED_URL + str).toList();
    }

}
