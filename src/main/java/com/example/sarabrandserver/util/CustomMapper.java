package com.example.sarabrandserver.util;

import com.example.sarabrandserver.product.entity.ProductDetail;
import com.example.sarabrandserver.product.entity.ProductSize;
import com.example.sarabrandserver.product.response.ColourResponse;
import com.example.sarabrandserver.product.response.SizeResponse;

import java.util.Set;
import java.util.stream.Collectors;

public class CustomMapper {

    public Set<ColourResponse> colourMapper(final Set<ProductDetail> set) {
        return set.stream()
                .flatMap(detail -> detail.getProductColours().stream()
                            .map(colour -> new ColourResponse(colour.getColour())))
                .collect(Collectors.toSet());
    }

    public Set<SizeResponse> sizeMapper(final Set<ProductSize> set) {
        return set.stream()
                .map(size -> new SizeResponse(size.getSize())) //
                .collect(Collectors.toSet());
    }

//    public Set<ImageResponse> imageMapper(final Set<ProductImage> set) {
//        return set.stream().map().collect(Collectors.toSet());
//    }

}
