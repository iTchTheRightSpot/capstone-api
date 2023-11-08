package com.sarabrandserver.product.projection;

import java.io.Serializable;

// Spring data projection
public interface DetailPojo extends Serializable {
    String getColour();
    Boolean getVisible();
    String getImage();
    String getVariants();
}
