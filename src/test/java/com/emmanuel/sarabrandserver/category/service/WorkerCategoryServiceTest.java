package com.emmanuel.sarabrandserver.category.service;

import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.emmanuel.sarabrandserver.category.entity.ProductCategory;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.exception.CustomNotFoundException;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.util.DateUTC;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:application-dev.properties")
class WorkerCategoryServiceTest {

    private WorkerCategoryService workerCategoryService;

    @Mock private CategoryRepository categoryRepository;

    @Mock private DateUTC dateUTC;

    @BeforeEach
    void setUp() {
        this.workerCategoryService = new WorkerCategoryService(this.categoryRepository, this.dateUTC);
    }

    /** Simulates creating a new ProductCategory when CategoryDTO param parent is empty */
    @Test
    void create() {
        // Given
        var dto = new CategoryDTO(new Faker().commerce().department(), true, "");

        var category = ProductCategory.builder()
                .categoryName(dto.getName().trim())
                .createAt(new Date())
                .productCategories(new HashSet<>())
                .product(new HashSet<>())
                .build();

        // When
        when(this.dateUTC.toUTC(any(Date.class))).thenReturn(Optional.of(new Date()));
        when(this.categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(this.categoryRepository.save(any(ProductCategory.class))).thenReturn(category);

        // Then
        assertEquals(CREATED, this.workerCategoryService.create(dto).getStatusCode());
        verify(this.categoryRepository, times(1)).save(any(ProductCategory.class));
    }

    /** Simulates creating a new ProductCategory when CategoryDTO param parent is non-empty */
    @Test
    void createParent() {
        // Given
        var dto = new CategoryDTO(new Faker().commerce().department(), true, new Faker().commerce().productName());

        var category = ProductCategory.builder()
                .categoryName(new Faker().commerce().department())
                .createAt(new Date())
                .productCategories(new HashSet<>())
                .product(new HashSet<>())
                .build();

        // When
        when(this.dateUTC.toUTC(any(Date.class))).thenReturn(Optional.of(new Date()));
        when(this.categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));
        when(this.categoryRepository.save(any(ProductCategory.class))).thenReturn(category);

        // Then
        assertEquals(CREATED, this.workerCategoryService.create(dto).getStatusCode());
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
                .categoryName(new Faker().commerce().department())
                .createAt(new Date())
                .productCategories(new HashSet<>())
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
        var dto = UpdateCategoryDTO.builder()
                .id(1L)
                .name("Updated category name")
                .build();

        // When
        doReturn(0).when(this.categoryRepository).duplicateCategoryForUpdate(anyString());
        when(this.dateUTC.toUTC(any(Date.class))).thenReturn(Optional.of(new Date()));

        // Then
        assertEquals(OK, this.workerCategoryService.update(dto).getStatusCode());
        verify(this.categoryRepository, times(1))
                .update(any(Date.class), anyLong(), anyString());
    }

    @Test
    void update_category_name_to_existing_name() {
        // Given
        var dto = UpdateCategoryDTO.builder()
                .id(1L)
                .name("Updated category name")
                .build();

        // When
        when(this.categoryRepository.duplicateCategoryForUpdate(anyString())).thenReturn(1);

        // Then
        assertThrows(DuplicateException.class, () -> this.workerCategoryService.update(dto));
    }

    @Test
    void delete() {
        // Given
        var category = ProductCategory.builder()
                .categoryName("Test Category")
                .createAt(new Date())
                .modifiedAt(null)
                .isVisible(true)
                .productCategories(new HashSet<>())
                .product(new HashSet<>())
                .build();

        // When
        doReturn(Optional.of(category)).when(this.categoryRepository).findByName(anyString());

        // Then
        assertEquals(NO_CONTENT, this.workerCategoryService.delete(category.getCategoryName()).getStatusCode());
        verify(this.categoryRepository, times(1)).delete(any(ProductCategory.class));
    }

}