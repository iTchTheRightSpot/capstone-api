package com.emmanuel.sarabrandserver.category.projection;

import java.util.Date;

// Using spring data projection
// Also using none primitives in-case of null
public interface CategoryPojo {
    Long getId();
    String getCategory();
    String getSub();
    Date getCreated();
    Date getModified();
    Boolean getVisible();
}
