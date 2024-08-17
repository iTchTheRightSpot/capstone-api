package dev.webserver.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.io.Serializable;
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

    public void addToChildren(final CategoryResponse child) {
        children.add(child);
    }
}
