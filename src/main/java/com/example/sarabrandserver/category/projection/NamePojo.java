package com.example.sarabrandserver.category.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface NamePojo {
    @JsonProperty(value = "category")
    String getCategory();
}
