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
        categoryService = new WorkerCategoryService(categoryRepository, s3Service);
    }

    /** Simulates creating a new ProductCategory when CategoryDTO param parentId is empty */
    @Test
    void create() {
        // Given
        var dto = new CategoryDto(new Faker().commerce().department(), true, null);

        var category = Category.builder()
                .name(dto.name().trim())
                .build();

        // When
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Then
        categoryService.create(dto);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    /** Simulates creating a new ProductCategory when CategoryDTO param parentId is non-empty */
    @Test
    void createParent() {
        // Given
        var dto = new CategoryDto(new Faker().commerce().department(), true, 1L);

        var category = Category.builder().name(new Faker().commerce().department()).build();

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
        var dto = new CategoryDto(new Faker().commerce().department(), true, null);

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
        var dto = new CategoryDto(new Faker().commerce().department(), true, null);

        var category = Category.builder().name(new Faker().commerce().department()).build();

        // When
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));

        // Then
        assertThrows(DuplicateException.class, () -> categoryService.create(dto));
    }

    @Test
    void update() {
        // Given
        var dto = new UpdateCategoryDto(1L, 0L, "update categoryId name", true);

        // When
        doReturn(0).when(categoryRepository)
                .onDuplicateCategoryName(anyLong(), anyString());

        // Then
        categoryService.update(dto);
        verify(categoryRepository, times(1)).update(anyString(), anyBoolean(), anyLong());
    }

    @Test
    void update_category_name_to_existing_name() {
        // Given
        var dto = new UpdateCategoryDto(1L, 0L,"update categoryId name", true);

        // When
        when(categoryRepository.onDuplicateCategoryName(anyLong(), anyString()))
                .thenReturn(1);

        // Then
        assertThrows(DuplicateException.class, () -> categoryService.update(dto));
    }

}