package com.sarabrandserver.category.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CategoryResponse(
        @JsonProperty(value = "category_id")
        long id,
        @JsonProperty(value = "parent_id")
        long parent,
        String name,
        boolean visible
) {

    public CategoryResponse(long id, long parent, String name) {
        this(id, parent, name, false);
    }

}
