package com.emmanuel.sarabrandserver.product.projection;

import com.emmanuel.sarabrandserver.product.entity.ProductImage;
import com.emmanuel.sarabrandserver.product.entity.ProductSku;

import java.util.Set;

// Spring data projection
public interface DetailPojo {
    String getColour();
    Boolean getVisible();
    Set<ProductImage> getImage();
    Set<ProductSku> getSkus();
}
