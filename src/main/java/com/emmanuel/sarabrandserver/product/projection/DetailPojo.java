package com.emmanuel.sarabrandserver.product.projection;

import java.util.List;

public interface DetailPojo {
    String getSku();
    boolean getVisible();
    String getSize();
    int getQty();
    String getColour();
    List<String> getKey();
}
