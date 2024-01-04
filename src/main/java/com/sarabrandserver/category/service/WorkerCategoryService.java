package com.sarabrandserver.category.service;

import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.category.response.CategoryResponse;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.exception.ResourceAttachedException;
import com.sarabrandserver.product.response.ProductResponse;
import com.sarabrandserver.util.CustomUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkerCategoryService {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    private final CategoryRepository categoryRepository;
    private final CustomUtil customUtil;
    private final S3Service s3Service;

    /**
     * Returns a lis of ProductCategory parameters.
     * @return List of CategoryResponse
     * */
    public List<CategoryResponse> allCategories() {
        return this.categoryRepository
                .fetchCategoriesWorker() //
                .stream() //
                .map(pojo -> CategoryResponse.builder()
                        .id(pojo.getUuid())
                        .category(pojo.getCategory())
                        .created(pojo.getCreated().getTime())
                        .modified(pojo.getModified() == null ? 0L : pojo.getModified().getTime())
                        .visible(pojo.getVisible())
                        .build()
                )
                .toList();
    }

    /**
     * Returns a page of ProductResponse based on category uuid
     * @param id category uuid
     * @param page pagination
     * @param size pagination
     * @return Page of ProductResponse
     * */
    public Page<ProductResponse> allProductsByCategory(SarreCurrency currency, String id, int page, int size) {
        return this.categoryRepository
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
     * The logic to creating a new ProductCategory object is a worker can either add dto.name (child ProductCategory)
     * to an existing dto.parent (parent ProductCategory) or create new ProductCategory who has no parent.
     * @param dto of type CategoryDTO
     * @throws DuplicateException when dto.name exists
     * @throws CustomNotFoundException when dto.parent (Parent Category) does not exist
     * */
    @Transactional
    public void create(CategoryDTO dto) {
        var date = this.customUtil.toUTC(new Date());

        // Handle cases based on the logic explained above.
        var category = dto.parent().isBlank()
                ? parentCategoryIsBlank(dto, date)
                : parentCategoryNotBlank(dto, date);

        this.categoryRepository.save(category);
    }

    private ProductCategory parentCategoryIsBlank(CategoryDTO dto, Date date) {
        if (this.categoryRepository.findByName(dto.name().trim()).isPresent()) {
            throw new DuplicateException(dto.name() + " exists");
        }

        return ProductCategory.builder()
                .uuid(UUID.randomUUID().toString())
                .categoryName(dto.name().trim())
                .isVisible(dto.visible())
                .createAt(date)
                .categories(new HashSet<>())
                .product(new HashSet<>())
                .build();
    }

    /**
     * Throws CustomNotFoundException if parent uuid does not exist
     * */
    private ProductCategory parentCategoryNotBlank(CategoryDTO dto, Date date) {
        var parent = findByName(dto.parent().trim());
        return ProductCategory.builder()
                .uuid(UUID.randomUUID().toString())
                .categoryName(dto.name().trim())
                .isVisible(dto.visible())
                .parentCategory(parent)
                .createAt(date)
                .categories(new HashSet<>())
                .product(new HashSet<>())
                .build();
    }

    /**
     * Method is responsible for updating a ProductCategory based on uuid.
     * @param dto of type UpdateCategoryDTO
     * @throws DuplicateException is thrown if category name exists but is not associated to uuid
     * */
    @Transactional
    public void update(UpdateCategoryDTO dto) {
        boolean bool = this.categoryRepository
                .duplicateCategoryForUpdate(dto.id().trim(), dto.name().trim()) > 0;

        if (bool) {
            throw new DuplicateException(dto.name() + " cannot be created. It is a duplicate");
        }

        var date = this.customUtil.toUTC(new Date());

        this.categoryRepository
                .update(date, dto.name().trim(), dto.visible(), dto.id());
    }

    /**
     * Method permanently deletes a ProductCategory and its children.
     * @param uuid is the ProductCategory uuid
     * @throws CustomNotFoundException is thrown if category node does not exist
     * */
    @Transactional
    public void delete(String uuid) {
        int count = this.categoryRepository.productsAttached(uuid);

        if (count > 0) {
            throw new ResourceAttachedException("Cannot delete category because it is attached to 1 or many products");
        }

        var category = findByUuid(uuid);
        this.categoryRepository.delete(category);
    }

    public ProductCategory findByName(String name) {
        return this.categoryRepository.findByName(name)
                .orElseThrow(() -> new CustomNotFoundException(name + " does not exist"));
    }

    public ProductCategory findByUuid(String uuid) {
        return this.categoryRepository.findByUuid(uuid)
                .orElseThrow(() -> new CustomNotFoundException("Product Category does not exist"));
    }

}
