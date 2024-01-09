package com.sarabrandserver.category.service;

import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.projection.CategoryPojo;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.category.response.CategoryResponse;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.exception.ResourceAttachedException;
import com.sarabrandserver.product.response.ProductResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerCategoryService {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    private final CategoryRepository categoryRepo;
    private final S3Service s3Service;

    /**
     * Returns a list of {@code CategoryResponse}
     * */
    public List<CategoryResponse> allCategories() {
        return this.categoryRepo
                .superCategories()
                .stream()
                .flatMap(cat -> this.categoryRepo
                        .all_categories_admin_front(cat.getCategoryId())
                        .stream()
                        .map(p -> new CategoryResponse(p.getId(), p.getParent(), p.getName(), p.statusImpl()))
                )
                .toList();
    }

    /**
     * Returns a page of ProductResponse based on categoryId uuid
     * @param id {@code ProductCategory} categoryId
     * @param page pagination
     * @param size pagination
     * @return Page of {@code ProductResponse}
     * */
    public Page<ProductResponse> allProductsByCategory(SarreCurrency currency, long id, int page, int size) {
        return this.categoryRepo
                .allProductsByCategory(currency, id, PageRequest.of(page, size))
                .map(pojo -> {
                    var url = this.s3Service.preSignedUrl(this.BUCKET, pojo.getKey());

                    return ProductResponse.builder()
                            .id(pojo.getUuid())
                            .name(pojo.getName())
                            .price(pojo.getPrice())
                            .currency(pojo.getCurrency())
                            .imageUrl(url)
                            .build();
                });
    }

    /**
     * The logic to creating a new ProductCategory object is a worker can either add dto.name
     * (child {@code ProductCategory}) to an existing dto.parent (parent {@code ProductCategory})
     * or create new ProductCategory who has no parent.
     *
     * @param dto of type CategoryDTO
     * @throws DuplicateException when dto.name exists
     * @throws CustomNotFoundException when dto.parent (Parent Category) does not exist
     * */
    @Transactional
    public void create(CategoryDTO dto) {
        var category = dto.parent().isBlank()
                ? parentCategoryIsBlank(dto)
                : parentCategoryNotBlank(dto);

        this.categoryRepo.save(category);
    }

    private ProductCategory parentCategoryIsBlank(CategoryDTO dto) {
        if (this.categoryRepo.findByName(dto.name().trim()).isPresent()) {
            throw new DuplicateException(dto.name() + " exists");
        }
        return ProductCategory.builder()
                .name(dto.name().trim())
                .isVisible(dto.visible())
                .categories(new HashSet<>())
                .product(new HashSet<>())
                .build();
    }

    /**
     * Throws CustomNotFoundException if parent uuid does not exist
     * */
    private ProductCategory parentCategoryNotBlank(CategoryDTO dto) {
        var parent = findByName(dto.parent().trim());
        return ProductCategory.builder()
                .name(dto.name().trim())
                .isVisible(dto.visible())
                .parentCategory(parent)
                .categories(new HashSet<>())
                .product(new HashSet<>())
                .build();
    }

    /**
     * Method is responsible for updating a ProductCategory based on uuid.
     * @param dto of type UpdateCategoryDTO
     * @throws DuplicateException is thrown if categoryId name exists but is not associated to uuid
     * */
    @Transactional
    public void update(UpdateCategoryDTO dto) {
        boolean bool = this.categoryRepo
                .onDuplicateCategoryName(dto.id(), dto.name().trim()) > 0;

        if (bool) {
            throw new DuplicateException(dto.name() + " is a duplicate");
        }

        // update all children categories to false
        if (!dto.visible()) {
            for (CategoryPojo pojo : categoryRepo.all_categories_admin_front(dto.id())) {
                categoryRepo.upVisibility(pojo.getId(), false);
            }
        }

        this.categoryRepo
                .update(dto.name().trim(), dto.visible(), dto.id());
    }

    /**
     * Permanently deletes a ProductCategory and its children.
     * @param id is the ProductCategory uuid
     * @throws CustomNotFoundException is thrown if categoryId node does not exist
     * */
    @Transactional
    public void delete(long id) {
        int c = this.categoryRepo.validateContainsSubCategory(id);
        int d = this.categoryRepo.validateProductAttached(id);

        if (c > 1 || d > 0) {
            throw new ResourceAttachedException("Category has 1 or many products or sub-categoryId attached");
        }

        var category = findById(id);
        this.categoryRepo.delete(category);
    }

    public ProductCategory findByName(String name) {
        return this.categoryRepo.findByName(name)
                .orElseThrow(() -> new CustomNotFoundException(name + " does not exist"));
    }

    public ProductCategory findById(long id) {
        return this.categoryRepo.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Product Category does not exist"));
    }

}
