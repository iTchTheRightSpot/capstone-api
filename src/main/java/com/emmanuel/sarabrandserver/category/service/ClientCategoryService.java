package com.emmanuel.sarabrandserver.category.service;

import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.response.CategoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientCategoryService {
    private final CategoryRepository categoryRepository;

    public ClientCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Returns a list of parent and child categories.
     * @return List of CategoryResponse
     * */
    public List<CategoryResponse> fetchAll() {
        return this.categoryRepository.fetchCategoriesClient()
                .stream() //
                .map(pojo -> new CategoryResponse(pojo.getCategory())) //
                .toList();
    }

}
