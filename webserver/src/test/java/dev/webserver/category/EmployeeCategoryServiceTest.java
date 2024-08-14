package dev.webserver.category;

import com.github.javafaker.Faker;
import dev.webserver.AbstractUnitTest;
import dev.webserver.cache.CacheEnum;
import dev.webserver.cache.CacheImpl;
import dev.webserver.exception.DuplicateException;
import dev.webserver.external.aws.IS3Service;
import dev.webserver.product.IProductCachePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

final class EmployeeCategoryServiceTest extends AbstractUnitTest {

    private EmployeeCategoryService categoryService;

    @Mock private Environment environment;
    @Mock private CategoryRepository categoryRepository;
    @Mock private IS3Service s3Service;
    @Mock private CacheImpl<CacheEnum, List<CategoryResponse>> allCategoriesCache;
    @Mock private IProductCachePublisher productCachePublisher;

    @BeforeEach
    void setUp() {
        categoryService = new EmployeeCategoryService(environment, categoryRepository, s3Service, allCategoriesCache, productCachePublisher);
    }

    /** Simulates creating a new ProductCategory when CategoryDTO param parentId is empty */
    @Test
    void create() {
        // Given
        final var dto = new CategoryDto(new Faker().commerce().department(), true, null);

        final var category = Category.builder()
                .name(dto.name().trim())
                .build();

        // When
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Then
        categoryService.create(dto);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    /**
     * Simulates creating a new Category when CategoryDTO param parentId is non-empty
     */
    @Test
    void createParent() {
        // Given
        final var dto = new CategoryDto(new Faker().commerce().department(), true, 1L);

        final var category = Category.builder().name(new Faker().commerce().department()).build();

        // When
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Then
        categoryService.create(dto);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void category_duplicate_name() {
        // Given
        final var dto = new CategoryDto(new Faker().commerce().department(), true, null);

        // When
        when(categoryRepository.findByName(anyString()))
                .thenReturn(Optional.of(Category.builder().name(dto.name()).build()));

        // Then
        assertThrows(DuplicateException.class, () -> categoryService.create(dto));
    }

    /**
     * simulates the correct exception class is thrown when category name exists
     * */
    @Test
    void duplicate() {
        // Given
        final var dto = new CategoryDto(new Faker().commerce().department(), true, null);

        final var category = Category.builder().name(new Faker().commerce().department()).build();

        // When
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));

        // Then
        assertThrows(DuplicateException.class, () -> categoryService.create(dto));
    }

    @Test
    void update() {
        // Given
        final var dto = new UpdateCategoryDto(1L, null, "update categoryId name", true);

        // When
        doReturn(Optional.of(Category.builder().categoryId(1L).build())).when(categoryRepository).findById(anyLong());
        doReturn(0).when(categoryRepository).onDuplicateCategoryName(anyLong(), anyString());

        // method to test
        categoryService.update(dto);

        // Then
        verify(categoryRepository, times(1)).update(anyString(), anyBoolean(), isNull(), any(long.class));
    }

}