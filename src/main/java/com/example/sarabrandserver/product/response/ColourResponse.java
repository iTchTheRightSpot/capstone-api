package com.example.sarabrandserver.product.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ColourResponse {
    private String colour;

    public ColourResponse(String colour) {
        this.colour = colour;
    }
}
