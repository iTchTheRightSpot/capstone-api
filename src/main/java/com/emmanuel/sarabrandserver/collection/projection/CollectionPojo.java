package com.emmanuel.sarabrandserver.collection.projection;

import java.util.Date;

public interface CollectionPojo {
    String getCollection();
    Date getCreated();
    Date getModified();
    boolean getVisible();
}
