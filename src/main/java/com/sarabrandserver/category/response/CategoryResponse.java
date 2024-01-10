package com.sarabrandserver.category.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
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

    public CategoryResponse(String name) {
        this(-1, -1L, name);
    }

    public CategoryResponse(long id, Long parent, String name, boolean visible) {
        this(id, parent, name, visible, new ArrayList<>());
    }

    public CategoryResponse(long id, Long parent, String name) {
        this(id, parent, name, false);
    }

    public void addToChildren(CategoryResponse child) {
        children.add(child);
    }

}
