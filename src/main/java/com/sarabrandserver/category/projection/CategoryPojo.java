package com.sarabrandserver.category.projection;

// Using spring data projection
// Also using none primitives in-case of null
public interface CategoryPojo {

    Long getId();
    String getName();
    Boolean getVisible();
    Long getParent();

}
