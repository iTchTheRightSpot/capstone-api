package dev.webserver.category;

import java.io.Serializable;
import java.util.List;

public record WorkerCategoryResponse(
        List<CategoryResponse> table,
        List<CategoryResponse> hierarchy
) implements Serializable { }