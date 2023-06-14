package com.example.sarabrandserver.category.service;

import com.example.sarabrandserver.category.dto.CategoryDTO;
import com.example.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.example.sarabrandserver.category.entity.ProductCategory;
import com.example.sarabrandserver.category.repository.CategoryRepository;
import com.example.sarabrandserver.category.response.CategoryResponse;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final DateUTC dateUTC;

    public CategoryService(CategoryRepository categoryRepository, DateUTC dateUTC) {
        this.categoryRepository = categoryRepository;
        this.dateUTC = dateUTC;
    }

    /**
     * Responsible for getting a list of parent category name and child category names. This approach is limited where
     * each parent category can only have one child and the child has no child category. In other words only a hierarchy
     * of 1 -> 1 or Parent -> Child
     * @return List of CategoryResponse
     * */
    public List<CategoryResponse> fetchAll() {
        return this.categoryRepository
                .getParentCategoriesWithIdNull()
                .stream() //
                .map(productCategory -> {
                    Object[] parentArr = (Object[]) productCategory;
                    // Get Children
                    var subCategory = this.categoryRepository.getChildCategoriesWhereDeletedIsNull((Long) parentArr[0]);
                    return new CategoryResponse((String) parentArr[1], subCategory);
                }) //
                .toList();
    }

    /**
     * Method is responsible for creating a new category.
     * A few gotchas to know before calling this API is sub-category can only be a size of one.
     *
     * @param dto of type CategoryDTO
     * @throws DuplicateException when a category name exists
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> create(CategoryDTO dto) {
        if (dto.sub_category().size() > 1) {
            return new ResponseEntity<>("Cannot have more than 1 sub-category", BAD_REQUEST);
        }

        if (this.categoryRepository.duplicateCategoryName(dto.category_name().trim(), dto.sub_category()) > 0) {
            throw new DuplicateException("Duplicate name or sub category");
        }

        var date = this.dateUTC.toUTC(new Date()).isEmpty() ? new Date() : this.dateUTC.toUTC(new Date()).get();

        // Parent Category
        var category = ProductCategory.builder()
                .categoryName(dto.category_name().trim())
                .createAt(date)
                .productCategories(new HashSet<>())
                .product(new HashSet<>())
                .build();

        // Sub Category
        dto.sub_category().forEach(str -> category.addCategory(new ProductCategory(str, date)));
        this.categoryRepository.save(category);
        return new ResponseEntity<>("created", CREATED);
    }

    /**
     * Method is responsible for updating a category name, and it also records the Date Category is modified.
     * @param dto of type UpdateCategoryDTO
     * @throws CustomNotFoundException is thrown if category name does not exist
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> update(UpdateCategoryDTO dto) {
        var category = findByName(dto.old_name().trim());
        var date = this.dateUTC.toUTC(new Date());

        if (date.isEmpty()) {
            return new ResponseEntity<>(
                    "Server error please try again or call developer if error persists", INTERNAL_SERVER_ERROR);
        }

        this.categoryRepository.update(date.get(), category.getCategoryName(), dto.old_name().trim());
        return new ResponseEntity<>("updated", OK);
    }

    /**
     * First things first a ProductCategory cannot be deleted because it has relationships with other tables, so
     * instead attribute deleted_at is set.
     * To delete a category from the DB, there are some parameters to be met before deleting.
     * 1) First validate if the category node exist.
     * 2)If ProductCategory exist (NOTE),
     *  i. The design right now is every parent node has no parent node but might have a max of one child node.
     * 3) Assume node is a parent node, delete child node before deleting parent.
     *
     * @param node is the ProductCategory name
     * @throws CustomNotFoundException is thrown if category node does not exist
     * @return ResponseEntity
     * */
    @Transactional
    public ResponseEntity<?> delete(String node) {
        // Return 400 if node is null or empty
        if (node == null || node.isEmpty()) {
            return new ResponseEntity<>("Product node cannot be null or empty", BAD_REQUEST);
        }

        // Get date in UTC
        var date = this.dateUTC.toUTC(new Date()).isEmpty() ? new Date() : this.dateUTC.toUTC(new Date()).get();

        // Find product by node
        var parent = findByName(node.trim());

        // Find all Children
        var children = this.categoryRepository.getChildCategoriesWhereDeletedIsNull(parent.getCategoryId());

        // Custom delete all children
        children.forEach(childName -> this.categoryRepository.custom_delete(date, childName));

        // Custom delete parent
        this.categoryRepository.custom_delete(date, parent.getCategoryName());

        return new ResponseEntity<>(NO_CONTENT);
    }

    public ProductCategory findByName(String name) {
        return this.categoryRepository.findByName(name)
                .orElseThrow(() -> new CustomNotFoundException("Does not exist"));
    }

    public void save(ProductCategory category) {
        this.categoryRepository.save(category);
    }

}
