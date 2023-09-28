package com.emmanuel.sarabrandserver.category.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CategoryResponse {

    @JsonProperty(value = "created_at")
    private long created;
    @JsonProperty(value = "modified_at")
    private long modified;

    @JsonProperty(value = "category_id")
    private String id;
    private String category;
    private boolean visible;
    private List<String> child;

    public CategoryResponse(String id, String category) {
        this.id = id;
        this.category = category;
    }

}
