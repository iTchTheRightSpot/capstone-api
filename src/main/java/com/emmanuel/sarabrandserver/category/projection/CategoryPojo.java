package com.emmanuel.sarabrandserver.category.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public interface CategoryPojo {
    @JsonProperty(value = "category_name")
    String getCategory();
    @JsonProperty(value = "status")
    boolean getStatus();
    @JsonProperty(value = "sub_category")
    List<String> getSub();
}
