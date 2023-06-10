package com.example.sarabrandserver.product.category.service;

import com.example.sarabrandserver.exception.CustomNotFoundException;
import com.example.sarabrandserver.exception.DuplicateException;
import com.example.sarabrandserver.product.category.dto.UpdateCategoryDTO;
import com.example.sarabrandserver.product.category.dto.CategoryDTO;
import com.example.sarabrandserver.product.category.entity.ProductCategory;
import com.example.sarabrandserver.product.category.repository.CategoryRepository;
import com.example.sarabrandserver.product.category.response.CategoryResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Responsible for getting a list of parent category name and child category names.
     *
     * @return List of CategoryResponse
     * */
    public List<?> fetchAll() {
        return this.categoryRepository
                .allCategoryName() //
                .stream()
                .map(productResponse -> {
                    var obj = (ProductCategory) productResponse;
                    return new CategoryResponse(
                            obj.getCategoryName(),
                            obj.getProductCategories().stream().map(ProductCategory::getCategoryName).toList()
                    );
                }).toList();
    }

    /**
     * Method is responsible for creating a new category.
     *
     * @param dto of type CategoryDTO
     * @throws DuplicateException when a category name exists
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> create(CategoryDTO dto) {
        if (this.categoryRepository.duplicateCategoryName(dto.category_name(), dto.sub_category()) > 0) {
            throw new DuplicateException("Duplicate name or sub category");
        }
        var category = new ProductCategory(dto.category_name());
        dto.sub_category().forEach(str -> category.addCategory(new ProductCategory(str)));
        this.categoryRepository.save(category);
        return new ResponseEntity<>("created", CREATED);
    }

    /**
     * To update a parent or child category name,
     *
     * @param dto of type UpdateCategoryDTO
     * @throws CustomNotFoundException is thrown if parent category name does not exist
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> update(UpdateCategoryDTO dto) {
        if (this.categoryRepository.findByName(dto.category_name()).isEmpty()) {
            throw new CustomNotFoundException("Does not exist");
        }


        return null;
    }

    // TODO a lot of validation to be made before deleting a category because of its relationship
    public ResponseEntity<?> delete(String id) {
        return null;
    }

}
