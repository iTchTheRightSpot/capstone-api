package com.example.sarabrandserver.category.service;

import com.example.sarabrandserver.category.projection.CategoryPojo;
import com.example.sarabrandserver.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientCategoryService {

    private final CategoryRepository categoryRepository;

    public ClientCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /** Returns a list of Parent and Child categories with visibility marked as true */
    public List<CategoryPojo> fetchAll() {
        return this.categoryRepository.fetchCategoriesClient();
    }

}
