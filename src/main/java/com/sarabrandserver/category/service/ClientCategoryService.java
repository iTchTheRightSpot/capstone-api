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
     * Returns a list of {@code CategoryResponse}
     * */
    public List<CategoryResponse> allCategories() {
        return this.repository
                .superCategories()
                .stream()
                .flatMap(cat -> this.repository
                        .all_categories_store_front(cat.getCategoryId())
                        .stream()
                        .map(p -> new CategoryResponse(p.getId(), p.getParent(), p.getName()))
                )
                .toList();
    }

}
