package dev.webserver.product;

// Spring data projection
public interface DetailProjection {

    String getColour();
    Boolean getVisible();
    String getImage();
    String getVariants();

}
