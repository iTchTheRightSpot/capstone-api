package dev.webserver.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryResponse(
        @JsonProperty(value = "category_id")
        long categoryId,
        @JsonProperty(value = "parent_id")
        Long parentId,
        String name,
        Boolean visible,
        List<CategoryResponse> children
) implements Serializable {

    public CategoryResponse(final String name) {
        this(-1, -1L, name);
    }

    public CategoryResponse(final long categoryId, final Long parentId, final String name, final boolean visible) {
        this(categoryId, parentId, name, visible, new ArrayList<>());
    }

    public CategoryResponse(final long categoryId, final Long parentId, final String name) {
        this(categoryId, parentId, name, false);
    }

    public void addToChildren(final CategoryResponse child) {
        children.add(child);
    }
}
