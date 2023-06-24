package com.emmanuel.sarabrandserver.category.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
