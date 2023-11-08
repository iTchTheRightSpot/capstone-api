package com.sarabrandserver.collection.projection;

import java.io.Serializable;
import java.util.Date;

public interface CollectionPojo extends Serializable {
    String getUuid();
    String getCollection();
    Date getCreated();
    Date getModified();
    Boolean getVisible();
}
