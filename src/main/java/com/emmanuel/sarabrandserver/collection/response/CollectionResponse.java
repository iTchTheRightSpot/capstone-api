package com.emmanuel.sarabrandserver.collection.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CollectionResponse {
    @JsonProperty(value = "created_at")
    private long created;
    @JsonProperty(value = "modified_at")
    private long modified;

    private String collection;
    private boolean visible;

    public CollectionResponse(String collection) {
        this.collection = collection;
    }
}
