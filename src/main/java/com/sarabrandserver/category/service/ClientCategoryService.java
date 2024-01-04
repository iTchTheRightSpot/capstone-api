package com.sarabrandserver.category.service;

import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.category.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientCategoryService {

    private final CategoryRepository repository;

    /**
     * Returns a list of parent and child categories.
     * @return List of CategoryResponse
     * */
    public List<CategoryResponse> allCategories() {
        return this.repository
                .fetchCategoriesClient()
                .stream() //
                .map(p -> new CategoryResponse(p.getUuid(), p.getCategory())) //
                .toList();
    }

}
