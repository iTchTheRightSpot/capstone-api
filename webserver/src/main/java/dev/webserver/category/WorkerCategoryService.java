package dev.webserver.category;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.exception.CustomNotFoundException;
import dev.webserver.exception.DuplicateException;
import dev.webserver.exception.ResourceAttachedException;
import dev.webserver.external.aws.IS3Service;
import dev.webserver.product.ProductProjection;
import dev.webserver.product.ProductResponse;
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
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class WorkerCategoryService {

    private static final Logger log = LoggerFactory.getLogger(WorkerCategoryService.class);

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    private final CategoryRepository categoryRepository;
    private final IS3Service service;

    /**
     * Returns a list {@link WorkerCategoryResponse}
     * */
    public WorkerCategoryResponse allCategories() {
        var category = this.categoryRepository.allCategories();

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
     * Retrieves a {@link Page} of {@link ProductProjection} objects from the
     * database and then asynchronously calls to return S3 to get all images for each product.
     *
     * @param currency    The currency in which prices are displayed.
     * @param categoryId  The primary key of a {@link Category}.
     * @param page        The page number for pagination.
     * @param size        The page size for pagination.
     * @return A {@link Page} of {@link ProductResponse}.
     */
    public Page<ProductResponse> allProductsByCategoryId(
            final SarreCurrency currency,
            final long categoryId,
            final int page,
            final int size
    ) {
        final var pageOfProducts = this.categoryRepository
                .allProductsByCategoryIdAdminFront(categoryId, currency, PageRequest.of(page, size));

        final var futures = pageOfProducts
                .stream()
                .map(p -> (Supplier<ProductResponse>) () -> ProductResponse.builder()
                        .id(p.getUuid())
                        .name(p.getName())
                        .price(p.getPrice())
                        .currency(p.getCurrency())
                        .imageUrl(service.preSignedUrl(BUCKET, p.getImage()))
                        .build()
                )
                .toList();

        final var products = CustomUtil.asynchronousTasks(futures).join();
        return new PageImpl<>(products, pageOfProducts.getPageable(), pageOfProducts.getTotalElements());
    }

    /**
     * The logic to creating a new {@link Category} object
     * is a worker can either add dto.name (child {@link Category})
     * to an existing dto.parentId (parentId {@link Category}) or
     * create new {@link Category} who has no parentId.
     *
     * @param dto of type {@link CategoryDto}.
     * @throws DuplicateException when dto.name exists.
     * @throws CustomNotFoundException when dto.parentId does not exist.
     * */
    @Transactional(rollbackFor = Exception.class)
    public void create(final CategoryDto dto) {
        if (categoryRepository.findByName(dto.name().trim()).isPresent()) {
            throw new DuplicateException(dto.name() + " exists");
        }

        final Category category = dto.parentId() == null
                ? parentCategoryIsNull(dto)
                : parentCategoryNotNull(dto);

        categoryRepository.save(category);
    }

    private Category parentCategoryIsNull(CategoryDto dto) {
        return Category.builder()
                .name(dto.name().trim())
                .isVisible(dto.visible())
                .categories(new HashSet<>())
                .product(new HashSet<>())
                .build();
    }

    private Category parentCategoryNotNull(CategoryDto dto) {
        var parent = findById(dto.parentId());
        return Category.builder()
                .name(dto.name().trim())
                .isVisible(dto.visible())
                .parentCategory(parent)
                .categories(new HashSet<>())
                .product(new HashSet<>())
                .build();
    }

    /**
     * Updates a {@link Category} based on categoryId.
     *
     * @param dto {@link  UpdateCategoryDto}.
     * @throws DuplicateException is thrown if name exists, and it is not associated to
     * categoryId.
     * */
    @Transactional(rollbackFor = Exception.class)
    public void update(final UpdateCategoryDto dto) {
        final boolean bool = categoryRepository.onDuplicateCategoryName(dto.id(), dto.name().trim()) > 0;

        if (bool) {
            throw new DuplicateException(dto.name() + " is a duplicate");
        }

        if (!dto.visible()) {
            categoryRepository.updateAllChildrenVisibilityToFalse(dto.id());
        }

        if (dto.parentId() != null) {
            categoryRepository.updateCategoryParentIdBasedOnCategoryId(dto.id(), dto.parentId());
        }

        categoryRepository.update(dto.name().trim(), dto.visible(), dto.id());
    }

    /**
     * Permanently deletes a {@link Category}.
     *
     * @param categoryId is primary key of a {@link Category}.
     * @throws org.springframework.dao.DataIntegrityViolationException if {@link Category}
     * has children entities attached to it.
     * */
    @Transactional(rollbackFor = Exception.class)
    public void delete(final long categoryId) {
        try {
            categoryRepository.deleteProductCategoryById(categoryId);
        } catch (DataIntegrityViolationException e) {
            log.error("tried deleting a category with children attached {}", e.getMessage());
            throw new ResourceAttachedException("resource attached to category");
        }
    }

    public Category findById(final long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomNotFoundException("does not exist"));
    }

}