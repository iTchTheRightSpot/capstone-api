package com.example.sarabrandserver.collection.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface CollectionPojo {

    @JsonProperty(value = "collection")
    String getCollection();
}
