package com.example.sarabrandserver.product.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ImageResponse {

    @JsonProperty(value = "media_type")
    private String mediaType;
    @JsonProperty(value = "bytes")
    private byte[] bytes;

}
