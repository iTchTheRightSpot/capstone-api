package com.emmanuel.sarabrandserver.category.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
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

    private String category;
    private boolean visible;
    private List<String> child;

    public CategoryResponse(String category, String child) {
        this.category = category;
        this.child = Arrays.stream(child.split(",")).toList();
    }

}
