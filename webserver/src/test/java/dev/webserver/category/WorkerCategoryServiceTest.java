package dev.webserver.category;

import com.github.javafaker.Faker;
import dev.webserver.AbstractUnitTest;
import dev.webserver.exception.DuplicateException;
import dev.webserver.external.aws.IS3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkerCategoryServiceTest extends AbstractUnitTest {

    private WorkerCategoryService categoryService;

    @Mock private CategoryRepository categoryRepository;
    @Mock private IS3Service s3Service;

    @BeforeEach
    void setUp() {
        this.categoryService = new WorkerCategoryService(this.categoryRepository, this.s3Service);
    }

    /** Simulates creating a new ProductCategory when CategoryDTO param parentId is empty */
    @Test
    void create() {
        // Given
        var dto = new CategoryDto(new Faker().commerce().department(), true, null);

        var category = Category.builder()
                .name(dto.name().trim())
                .categories(new HashSet<>())
                .product(new HashSet<>())
                .build();

        // When
        when(this.categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(this.categoryRepository.save(any(Category.class))).thenReturn(category);

        // Then
        this.categoryService.create(dto);
        verify(this.categoryRepository, times(1)).save(any(Category.class));
    }

    /** Simulates creating a new ProductCategory when CategoryDTO param parentId is non-empty */
    @Test
    void createParent() {
        // Given
        var dto = new CategoryDto(new Faker().commerce().department(), true, 1L);

        var category = Category.builder()
                .name(new Faker().commerce().department())
                .categories(new HashSet<>())
                .product(new HashSet<>())
                .build();

        // When
        when(this.categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(this.categoryRepository.save(any(Category.class))).thenReturn(category);

        // Then
        this.categoryService.create(dto);
        verify(this.categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void category_duplicate_name() {
        // Given
        var dto = new CategoryDto(new Faker().commerce().department(), true, null);

        // When
        when(this.categoryRepository.findByName(anyString()))
                .thenReturn(
                        Optional.of(Category.builder().name(dto.name()).build())
                );

        // Then
        assertThrows(DuplicateException.class, () -> this.categoryService.create(dto));
    }

    /**
     * simulates the correct exception class is thrown when category name exists
     * */
    @Test
    void duplicate() {
        // Given
        var dto = new CategoryDto(new Faker().commerce().department(), true, null);

        var category = Category.builder()
                .name(new Faker().commerce().department())
                .categories(new HashSet<>())
                .product(new HashSet<>())
                .build();

        // When
        when(this.categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));

        // Then
        assertThrows(DuplicateException.class, () -> this.categoryService.create(dto));
    }

    @Test
    void update() {
        // Given
        var dto = new UpdateCategoryDto(1L, 0L, "update categoryId name", true);

        // When
        doReturn(0).when(this.categoryRepository)
                .onDuplicateCategoryName(anyLong(), anyString());

        // Then
        this.categoryService.update(dto);
        verify(this.categoryRepository, times(1))
                .update(anyString(), anyBoolean(), anyLong());
    }

    @Test
    void update_category_name_to_existing_name() {
        // Given
        var dto = new UpdateCategoryDto(1L, 0L,"update categoryId name", true);

        // When
        when(this.categoryRepository.onDuplicateCategoryName(anyLong(), anyString()))
                .thenReturn(1);

        // Then
        assertThrows(DuplicateException.class, () -> this.categoryService.update(dto));
    }

}