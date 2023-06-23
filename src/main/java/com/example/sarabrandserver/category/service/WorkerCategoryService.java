package com.example.sarabrandserver.category.service;

import com.example.sarabrandserver.category.dto.CategoryDTO;
import com.example.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.example.sarabrandserver.category.entity.ProductCategory;
import com.example.sarabrandserver.category.projection.CategoryPojo;
import com.example.sarabrandserver.category.projection.NamePojo;
import com.example.sarabrandserver.category.repository.CategoryRepository;
import com.example.sarabrandserver.exception.CustomNotFoundException;
import com.example.sarabrandserver.exception.DuplicateException;
import com.example.sarabrandserver.util.DateUTC;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
public class WorkerCategoryService {
    private final CategoryRepository categoryRepository;
    private final DateUTC dateUTC;

    public WorkerCategoryService(CategoryRepository categoryRepository, DateUTC dateUTC) {
        this.categoryRepository = categoryRepository;
        this.dateUTC = dateUTC;
    }

    /**
     * Responsible for getting a list of parent category name and child category names.
     * @return List of CategoryPojo
     * */
    public List<NamePojo> fetchAll() {
        return this.categoryRepository.fetchCategoriesWorker();
    }

    /**
     * Method is responsible for creating a new category.
     * @param dto of type CategoryDTO
     * @throws DuplicateException when a category name exists
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> create(CategoryDTO dto) {
        boolean bool = this.categoryRepository
                .duplicateCategoryName(dto.getCategory_name().trim(), dto.getSub_category()) > 0;

        if (bool || dto.getSub_category().contains(dto.getCategory_name())) {
            throw new DuplicateException("Duplicate name or sub category");
        }

        var date = this.dateUTC.toUTC(new Date()).isEmpty() ? new Date() : this.dateUTC.toUTC(new Date()).get();

        // Parent Category
        var category = ProductCategory.builder()
                .categoryName(dto.getCategory_name().trim())
                .isVisible(dto.getStatus())
                .createAt(date)
                .modifiedAt(null)
                .productCategories(new HashSet<>())
                .product(new HashSet<>())
                .build();

        // Persist sub-category to Category
        dto.getSub_category().forEach(str -> {
            var sub = ProductCategory.builder()
                    .categoryName(dto.getCategory_name().trim())
                    .isVisible(dto.getStatus())
                    .createAt(date)
                    .modifiedAt(null)
                    .productCategories(new HashSet<>())
                    .product(new HashSet<>())
                    .build();
            category.addCategory(sub);
        });

        this.categoryRepository.save(category);
        return new ResponseEntity<>("created", CREATED);
    }

    /**
     * Method is responsible for updating a ProductCategory.
     * @param dto of type UpdateCategoryDTO
     * @throws CustomNotFoundException is thrown if category name does not exist
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> update(UpdateCategoryDTO dto) {
        if (this.categoryRepository.duplicateCategoryForUpdate(dto.getNew_name().trim()) > 0) {
            return new ResponseEntity<>(dto.getNew_name() + " is a duplicate", CONFLICT);
        }

        var category = findByName(dto.getOld_name().trim());
        var date = this.dateUTC.toUTC(new Date()).isEmpty() ? new Date() : this.dateUTC.toUTC(new Date()).get();
        this.categoryRepository.update(date, category.getCategoryName(), dto.getNew_name().trim());
        return new ResponseEntity<>("updated", OK);
    }

    /**
     * Method permanently deletes a ProductCategory and SubCategories if exists
     * @param node is the ProductCategory name
     * @throws CustomNotFoundException is thrown if category node does not exist
     * @return ResponseEntity
     * */
    @Transactional
    public ResponseEntity<?> delete(String node) {
        var category = findByName(node);
        this.categoryRepository.delete(category);
        return new ResponseEntity<>(NO_CONTENT);
    }

    public ProductCategory findByName(String name) {
        return this.categoryRepository.findByName(name)
                .orElseThrow(() -> new CustomNotFoundException(name + " does not exist"));
    }

    public void save(ProductCategory category) {
        this.categoryRepository.save(category);
    }

}
