package com.sarabrandserver.category.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CategoryResponse(
        @JsonProperty(value = "category_id")
        long id,
        @JsonProperty(value = "parent_id")
        Long parent,
        String name,
        boolean visible,
        List<CategoryResponse> children
) {

    public CategoryResponse(long id, long parent, String name, boolean visible) {
        this(id, parent, name, visible, null);
    }

    public CategoryResponse(long id, long parent, String name) {
        this(id, parent, name, false);
    }

}
