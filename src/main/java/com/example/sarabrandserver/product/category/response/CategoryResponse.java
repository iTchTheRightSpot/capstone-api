package com.example.sarabrandserver.product.category.response;

import java.util.List;

public record CategoryResponse(String category_name, List<String> sub_category) {}
