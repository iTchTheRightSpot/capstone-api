package com.emmanuel.sarabrandserver.product.projection;

// Spring data projection
public interface DetailPojo {
    String getSku();
    Boolean getVisible();
    String getSize();
    Integer getQty();
    String getColour();
    String getKey(); // image keys
}
