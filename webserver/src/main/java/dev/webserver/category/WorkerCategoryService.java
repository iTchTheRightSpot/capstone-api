package dev.webserver.category;

import dev.webserver.external.aws.S3Service;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.exception.CustomNotFoundException;
import dev.webserver.exception.DuplicateException;
import dev.webserver.exception.ResourceAttachedException;
import dev.webserver.product.response.ProductResponse;
import dev.webserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class WorkerCategoryService {

    private static final Logger log = LoggerFactory.getLogger(WorkerCategoryService.class);

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    private final CategoryRepository repository;
    private final S3Service service;

    /**
     * Returns a list {@link WorkerCategoryResponse}
     * */
    public WorkerCategoryResponse allCategories() {
        var category = this.repository.allCategories();

        // table
        var table = category
                .stream()
                .map(CategoryResponse::workerList)
                .toList();

        // hierarchy
        var hierarchy = category
                .stream()
                .map(p -> new CategoryResponse(p.getId(), p.getParent(), p.getName(), p.statusImpl()))
                .toList();

        return new WorkerCategoryResponse(table, CustomUtil.createCategoryHierarchy(hierarchy));
    }

    /**
     * Asynchronously retrieves a {@link Page} of
     * {@link ProductResponse} objects associated with a
     * specific category.
     *
     * @param currency    The currency in which prices are displayed.
     * @param categoryId  The primary key of a {@link ProductCategory}.
     * @param page        The page number for pagination.
     * @param size        The page size for pagination.
     * @return A {@link CompletableFuture} representing a {@link Page}
     * of {@link ProductResponse}.
     */
    public CompletableFuture<Page<ProductResponse>> allProductsByCategoryId(
            SarreCurrency currency,
            long categoryId,
            int page,
            int size
    ) {
        var pageOfProducts = this.repository
                .allProductsByCategoryIdAdminFront(categoryId, currency, PageRequest.of(page, size));

        var futures = pageOfProducts.stream()
                .map(p -> (Supplier<ProductResponse>) () -> new ProductResponse(
                        p.getUuid(),
                        p.getName(),
                        p.getPrice(),
                        p.getCurrency(),
                        service.preSignedUrl(BUCKET, p.getImage())
                ))
                .toList();


        return CustomUtil.asynchronousTasks(futures, WorkerCategoryService.class)
                .thenApply(v -> new PageImpl<>(
                        v.stream().map(Supplier::get).toList(),
                        pageOfProducts.getPageable(),
                        pageOfProducts.getTotalElements()
                ));
    }

    /**
     * The logic to creating a new {@link ProductCategory} object
     * is a worker can either add dto.name (child {@link ProductCategory})
     * to an existing dto.parentId (parentId {@link ProductCategory}) or
     * create new {@link ProductCategory} who has no parentId.
     *
     * @param dto of type {@link CategoryDTO}.
     * @throws DuplicateException when dto.name exists.
     * @throws CustomNotFoundException when dto.parentId does not exist.
     * */
    public void create(CategoryDTO dto) {
        if (this.repository.findByName(dto.name().trim()).isPresent()) {
            throw new DuplicateException(dto.name() + " exists");
        }

        var category = dto.parentId() == null
                ? parentCategoryIsNull(dto)
                : parentCategoryNotNull(dto);

        this.repository.save(category);
    }

    private ProductCategory parentCategoryIsNull(CategoryDTO dto) {
        return ProductCategory.builder()
                .name(dto.name().trim())
                .isVisible(dto.visible())
                .categories(new HashSet<>())
                .product(new HashSet<>())
                .build();
    }

    private ProductCategory parentCategoryNotNull(CategoryDTO dto) {
        var parent = findById(dto.parentId());
        return ProductCategory.builder()
                .name(dto.name().trim())
                .isVisible(dto.visible())
                .parentCategory(parent)
                .categories(new HashSet<>())
                .product(new HashSet<>())
                .build();
    }

    /**
     * Updates a {@link ProductCategory} based on categoryId.
     *
     * @param dto {@link  UpdateCategoryDTO}.
     * @throws DuplicateException is thrown if name exists, and it is not associated to
     * categoryId.
     * */
    public void update(UpdateCategoryDTO dto) {
        boolean bool = this.repository
                .onDuplicateCategoryName(dto.id(), dto.name().trim()) > 0;

        if (bool) {
            throw new DuplicateException(dto.name() + " is a duplicate");
        }

        if (!dto.visible()) {
            repository.updateAllChildrenVisibilityToFalse(dto.id());
        }

        if (dto.parentId() != null) {
            repository
                    .updateCategoryParentIdBasedOnCategoryId(dto.id(), dto.parentId());
        }

        this.repository
                .update(dto.name().trim(), dto.visible(), dto.id());
    }

    /**
     * Permanently deletes a {@link ProductCategory}.
     *
     * @param id is {@code ProductCategory} categoryId
     * @throws org.springframework.dao.DataIntegrityViolationException if {@code ProductCategory}
     * has children entities attached to it.
     * */
    public void delete(final long id) {
        try {
            this.repository.deleteProductCategoryById(id);
        } catch (DataIntegrityViolationException e) {
            log.error("tried deleting a category with children attached {}", e.getMessage());
            throw new ResourceAttachedException("resource attached to category");
        }
    }

    public ProductCategory findById(final long id) {
        return this.repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("does not exist"));
    }

}