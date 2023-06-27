package com.emmanuel.sarabrandserver.category.projection;

import java.util.Date;

public interface CategoryPojo {
    String getCategory();
    String getSub();
    Date getCreated();
    Date getModified();
    boolean getVisible();
}
