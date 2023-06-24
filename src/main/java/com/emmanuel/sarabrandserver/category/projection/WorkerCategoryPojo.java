package com.emmanuel.sarabrandserver.category.projection;

import java.util.Date;

public interface WorkerCategoryPojo {
    String getCategory();
    Date getCreated();
    Date getModified();
    boolean getVisible();
}
