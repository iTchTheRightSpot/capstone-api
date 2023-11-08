package com.sarabrandserver.category.projection;

import java.io.Serializable;
import java.util.Date;

// Using spring data projection
// Also using none primitives in-case of null
public interface CategoryPojo extends Serializable {
    String getUuid();
    String getCategory();
    String getSub();
    Date getCreated();
    Date getModified();
    Boolean getVisible();
}
