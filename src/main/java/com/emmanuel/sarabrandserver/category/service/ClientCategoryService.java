package com.emmanuel.sarabrandserver.category.service;

import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientCategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Returns a list of parent and child categories.
     * @return List of CategoryResponse
     * */
    public List<CategoryResponse> fetchAll() {
        return this.categoryRepository.fetchCategoriesClient()
                .stream() //
                .map(pojo -> new CategoryResponse(pojo.getUuid(), pojo.getCategory())) //
                .toList();
    }

}
