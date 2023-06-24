package com.emmanuel.sarabrandserver.category.service;

import com.emmanuel.sarabrandserver.category.projection.CategoryPojo;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.product.projection.ClientProductPojo;
import org.springframework.data.domain.PageRequest;
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

    /**
     * Fetches a list of Products based on category name
     * @param name is ProductCategory name
     * @return List of CategoryPojo
     * */
    public List<ClientProductPojo> fetchProductOnCategory(final String name, final int page, final int size) {
        return this.categoryRepository.fetchByProductName(name, PageRequest.of(page, Math.min(size, 30)));
    }

}
