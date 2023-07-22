package com.emmanuel.sarabrandserver.category.service;

import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.emmanuel.sarabrandserver.category.entity.ProductCategory;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.response.CategoryResponse;
import com.emmanuel.sarabrandserver.exception.CustomNotFoundException;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.util.CustomUtil;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;

import static org.springframework.http.HttpStatus.*;

@Service
public class WorkerCategoryService {
    private final CategoryRepository categoryRepository;
    private final CustomUtil customUtil;

    public WorkerCategoryService(CategoryRepository categoryRepository, CustomUtil customUtil) {
        this.categoryRepository = categoryRepository;
        this.customUtil = customUtil;
    }

    /**
     * Returns a lis of ProductCategory parameters.
     * @return List of CategoryResponse
     * */
    public Page<CategoryResponse> fetchAll(int page, int size) {
        return this.categoryRepository
                .fetchCategoriesWorker(PageRequest.of(page, size))
                .map(pojo -> CategoryResponse.builder()
                        .id(pojo.getId())
                        .category(pojo.getCategory())
                        .created(pojo.getCreated().getTime())
                        .modified(pojo.getModified() == null ? 0L : pojo.getModified().getTime())
                        .visible(pojo.getVisible())
                        .build()
                );
    }

    /**
     * The logic to creating a new ProductCategory object is a worker can either add dto.name (child ProductCategory)
     * to an existing dto.parent (parent ProductCategory) or create new ProductCategory who has no parent.
     * @param dto of type CategoryDTO
     * @throws DuplicateException when dto.name exists
     * @throws CustomNotFoundException when dto.parent (Parent Category) does not exist
     * @return ResponseEntity of HttpStatus
     * */
    @Transactional
    public ResponseEntity<?> create(CategoryDTO dto) {
        var date = this.customUtil.toUTC(new Date()).orElse(new Date());

        // Handle cases based on the logic explained above.
        var category = dto.getParent().isBlank() ?
                parentCategoryIsBlank(dto, date) : parentCategoryNotBlank(dto, date);

        this.categoryRepository.save(category);
        return new ResponseEntity<>(CREATED);
    }

    private ProductCategory parentCategoryIsBlank(CategoryDTO dto, Date date) {
        if (this.categoryRepository.findByName(dto.getName().trim()).isPresent()) {
            throw new DuplicateException(dto.getName() + " exists");
        }

        return ProductCategory.builder()
                .categoryName(dto.getName().trim())
                .isVisible(dto.getVisible())
                .createAt(date)
                .modifiedAt(null)
                .productCategories(new HashSet<>())
                .product(new HashSet<>())
                .build();
    }

    private ProductCategory parentCategoryNotBlank(CategoryDTO dto, Date date) {
        var parentCategory = findByName(dto.getParent().trim());
        parentCategory.setModifiedAt(date);

        var childCategory = ProductCategory.builder()
                .categoryName(dto.getName().trim())
                .isVisible(dto.getVisible())
                .createAt(date)
                .modifiedAt(null)
                .productCategories(new HashSet<>())
                .product(new HashSet<>())
                .build();

        // Add child category to parent
        // addCategory automatically persists child to the database
        parentCategory.addCategory(childCategory);

        return parentCategory;
    }

    /**
     * Method is responsible for updating a ProductCategory based on its ID.
     * @param dto of type UpdateCategoryDTO
     * @throws DuplicateException is thrown if category name exist
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> update(UpdateCategoryDTO dto) {
        if (this.categoryRepository.duplicateCategoryForUpdate(dto.getName().trim()) > 0) {
            throw new DuplicateException(dto.getName() + " cannot be created. It is a duplicate");
        }

        var date = this.customUtil.toUTC(new Date()).orElse(new Date());

        this.categoryRepository.update(date, dto.getId(), dto.getName().trim());
        return new ResponseEntity<>(OK);
    }

    /**
     * Method permanently deletes a ProductCategory and its children.
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

    /** Needed in WorkerProductService class */
    public void save(ProductCategory category) {
        this.categoryRepository.save(category);
    }

}
