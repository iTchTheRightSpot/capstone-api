package com.example.sarabrandserver.category.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CategoryResponse(
        @JsonProperty("category_name") String category_name,
        @JsonProperty("sub_category")  List<String> sub_category
) {}
