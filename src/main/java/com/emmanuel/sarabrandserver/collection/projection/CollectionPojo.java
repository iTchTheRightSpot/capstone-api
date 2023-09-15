package com.emmanuel.sarabrandserver.collection.projection;

import java.util.Date;

public interface CollectionPojo {
    String getUuid();
    String getCollection();
    Date getCreated();
    Date getModified();
    Boolean getVisible();
}
