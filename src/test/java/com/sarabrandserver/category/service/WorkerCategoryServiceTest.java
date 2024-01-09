package com.sarabrandserver.category.service;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractUnitTest;
import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.DuplicateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkerCategoryServiceTest extends AbstractUnitTest {

    private WorkerCategoryService workerCategoryService;

    @Mock private CategoryRepository categoryRepository;
    @Mock private S3Service s3Service;

    @BeforeEach
    void setUp() {
        this.workerCategoryService = new WorkerCategoryService(this.categoryRepository, this.s3Service);
    }

    /** Simulates creating a new ProductCategory when CategoryDTO param parent is empty */
    @Test
    void create() {
        // Given
        var dto = new CategoryDTO(new Faker().commerce().department(), true, "");

        var category = ProductCategory.builder()
                .name(dto.name().trim())
                .categories(new HashSet<>())
                .product(new HashSet<>())
                .build();

        // When
        when(this.categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(this.categoryRepository.save(any(ProductCategory.class))).thenReturn(category);

        // Then
        this.workerCategoryService.create(dto);
        verify(this.categoryRepository, times(1)).save(any(ProductCategory.class));
    }

    /** Simulates creating a new ProductCategory when CategoryDTO param parent is non-empty */
    @Test
    void createParent() {
        // Given
        var dto = new CategoryDTO(new Faker().commerce().department(), true, new Faker().commerce().productName());

        var category = ProductCategory.builder()
                .name(new Faker().commerce().department())
                .categories(new HashSet<>())
                .product(new HashSet<>())
                .build();

        // When
        when(this.categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));
        when(this.categoryRepository.save(any(ProductCategory.class))).thenReturn(category);

        // Then
        this.workerCategoryService.create(dto);
        verify(this.categoryRepository, times(1)).save(any(ProductCategory.class));
    }

    /** Simulates the correct exception class is thrown for the private method parentCategoryNotBlank. */
    @Test
    void duplicate_name() {
        // Given
        var dto = new CategoryDTO(new Faker().commerce().department(), true, new Faker().commerce().department());

        // When
        when(this.categoryRepository.findByName(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(CustomNotFoundException.class, () -> this.workerCategoryService.create(dto));
    }

    /** Simulates the correct exception class is thrown for duplicate parentCategoryIsBlank method. */
    @Test
    void duplicate() {
        // Given
        var dto = new CategoryDTO(new Faker().commerce().department(), true, "");

        var category = ProductCategory.builder()
                .name(new Faker().commerce().department())
                .categories(new HashSet<>())
                .product(new HashSet<>())
                .build();

        // When
        when(this.categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));

        // Then
        assertThrows(DuplicateException.class, () -> this.workerCategoryService.create(dto));
    }

    @Test
    void update() {
        // Given
        var dto = new UpdateCategoryDTO(1L, "update categoryId name", true);

        // When
        doReturn(0).when(this.categoryRepository)
                .onDuplicateCategoryName(anyLong(), anyString());

        // Then
        this.workerCategoryService.update(dto);
        verify(this.categoryRepository, times(1))
                .update(anyString(), anyBoolean(), anyLong());
    }

    @Test
    void update_category_name_to_existing_name() {
        // Given
        var dto = new UpdateCategoryDTO(1L, "update categoryId name", true);

        // When
        when(this.categoryRepository.onDuplicateCategoryName(anyLong(), anyString()))
                .thenReturn(1);

        // Then
        assertThrows(DuplicateException.class, () -> this.workerCategoryService.update(dto));
    }

}