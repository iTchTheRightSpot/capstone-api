package dev.webserver.category;

import dev.webserver.AbstractEnvironment;
import dev.webserver.enumeration.CapstoneCurrency;
import dev.webserver.exception.CustomBadRequestException;
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
import java.util.Objects;
import java.util.function.Supplier;

@Service
public class EmployeeCategoryService extends AbstractEnvironment {

    private static final Logger log = LoggerFactory.getLogger(EmployeeCategoryService.class);

    private final CategoryRepository categoryRepository;
    private final IS3Service service;

    protected EmployeeCategoryService(final Environment environment, final CategoryRepository categoryRepository, final IS3Service service) {
        super(environment);
        this.categoryRepository = categoryRepository;
        this.service = service;
    }

    public List<CategoryResponse> allCategories() {
        return categoryRepository.allCategories()
                .stream()
                .map(c -> CategoryResponse.builder()
                        .categoryId(c.categoryId())
                        .name(c.name())
                        .visible(c.isVisible())
                        .parentId(c.parentId())
                        .children(null)
                        .build())
                .toList();
    }

    public Pageable<ProductResponse> allProductsByCategoryId(
            final CapstoneCurrency currency,
            final long categoryId,
            final int page,
            final int size
    ) {
        final Page of = Page.of(page, size);
        final int count = categoryRepository.countAllProductsByCategoryIdAdminFront(categoryId);
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

    @Transactional(rollbackFor = Exception.class)
    public void update(final UpdateCategoryDto dto) {
        final String newCategory = dto.name().trim();
        if (Objects.equals(dto.categoryId(), dto.parentId()))
            throw new CustomBadRequestException("category id and parent id cannot be the same");

        final var category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new CustomNotFoundException("category id not found"));

        if (categoryRepository.onDuplicateCategoryName(category.categoryId(), newCategory) > 0)
            throw new DuplicateException(dto.name() + " is a duplicate");

        Long parentId = dto.parentId();

        if (dto.parentId() != null)
            parentId = categoryRepository.findById(dto.parentId())
                    .orElseThrow(() -> new CustomNotFoundException("category parent id not found")).parentId();

        final boolean bool = dto.visible() == null ? category.isVisible() : dto.visible();

        categoryRepository.update(newCategory, bool, parentId, category.categoryId());

        if (!bool) categoryRepository.updateCategoryAndAllItsChildrenVisibilityToFalse(dto.categoryId());
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