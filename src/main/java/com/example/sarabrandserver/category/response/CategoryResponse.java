package com.example.sarabrandserver.category.response;

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

    @JsonProperty(value = "category_name")
    private String category_name;

    @JsonProperty(value = "sub_category")
    private List<String> sub_category;

}
