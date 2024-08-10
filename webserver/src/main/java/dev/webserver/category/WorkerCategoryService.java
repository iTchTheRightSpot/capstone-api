package dev.webserver.category;

import dev.webserver.AbstractEnvironment;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.exception.CustomNotFoundException;
import dev.webserver.exception.DuplicateException;
import dev.webserver.exception.ResourceAttachedException;
import dev.webserver.external.aws.IS3Service;
import dev.webserver.product.ProductResponse;
import dev.webserver.util.CustomUtil;
import dev.webserver.util.Page;
import dev.webserver.util.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

@Service
public class WorkerCategoryService extends AbstractEnvironment {

    private static final Logger log = LoggerFactory.getLogger(WorkerCategoryService.class);

    private final CategoryRepository categoryRepository;
    private final IS3Service service;

    protected WorkerCategoryService(final Environment environment, final CategoryRepository categoryRepository, final IS3Service service) {
        super(environment);
        this.categoryRepository = categoryRepository;
        this.service = service;
    }

    public List<Category> allCategories() {
        return categoryRepository.allCategories();
    }

    public Pageable<ProductResponse> allProductsByCategoryId(
            final SarreCurrency currency,
            final long categoryId,
            final int page,
            final int size
    ) {
        final Page of = Page.of(page, size);
        final Integer count = categoryRepository.countAllProductsByCategoryIdAdminFront(categoryId, currency);
        final var listOfProducts = categoryRepository.allProductsByCategoryIdAdminFront(categoryId, currency, of);

        final var futures = listOfProducts
                .stream()
                .map(p -> (Supplier<ProductResponse>) () -> ProductResponse.builder()
                        .id(p.uuid())
                        .name(p.name())
                        .price(p.price())
                        .currency(p.currency().name())
                        .imageKey(service.preSignedUrl(super.awsbucket, p.imageKey()))
                        .build()
                )
                .toList();

        final var products = CustomUtil.asynchronousTasks(futures).join();
        return new Pageable<>(of, count, products);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(final CategoryDto dto) {
        if (categoryRepository.findByName(dto.name().trim()).isPresent()) {
            throw new DuplicateException(dto.name() + " exists");
        }

        final Category category = dto.parentId() == null
                ? Category.builder().name(dto.name().trim()).isVisible(dto.visible()).build()
                : Category.builder().name(dto.name().trim()).isVisible(dto.visible()).parentId(findById(dto.parentId()).categoryId()).build();

        categoryRepository.save(category);
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
            categoryRepository.updateCategoryParentId(dto.id(), dto.parentId());
        }

        categoryRepository.update(dto.name().trim(), dto.visible(), dto.id());
    }

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
                .orElseThrow(() -> new CustomNotFoundException("category id not found"));
    }

}