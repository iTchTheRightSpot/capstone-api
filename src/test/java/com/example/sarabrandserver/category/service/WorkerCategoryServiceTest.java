package com.example.sarabrandserver.category.service;

import com.example.sarabrandserver.category.dto.CategoryDTO;
import com.example.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.example.sarabrandserver.category.entity.ProductCategory;
import com.example.sarabrandserver.category.repository.CategoryRepository;
import com.example.sarabrandserver.exception.CustomNotFoundException;
import com.example.sarabrandserver.exception.DuplicateException;
import com.example.sarabrandserver.util.DateUTC;
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
import java.util.UUID;

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

    @Test
    void create() {
        // Given
        var dto = new CategoryDTO(UUID.randomUUID().toString(), true, new HashSet<>());
        for (int i = 0; i < 50; i++) dto.getSub_category().add(new Faker().commerce().productName());

        var category = ProductCategory.builder()
                .categoryName(dto.getCategory_name().trim())
                .createAt(new Date())
                .productCategories(new HashSet<>())
                .product(new HashSet<>())
                .build();

        dto.getSub_category().forEach(str -> {
            var sub = ProductCategory.builder()
                    .categoryName(dto.getCategory_name().trim())
                    .isVisible(dto.getStatus())
                    .createAt(category.getCreateAt())
                    .modifiedAt(null)
                    .productCategories(new HashSet<>())
                    .product(new HashSet<>())
                    .build();
            category.addCategory(sub);
        });

        // When
        when(this.categoryRepository.duplicateCategoryName(dto.getCategory_name(), dto.getSub_category()))
                .thenReturn(0);
        when(this.dateUTC.toUTC(any(Date.class))).thenReturn(Optional.of(new Date()));
        when(this.categoryRepository.save(any(ProductCategory.class))).thenReturn(category);

        // Then
        var res = this.workerCategoryService.create(dto);
        assertEquals(CREATED, res.getStatusCode());
        verify(this.categoryRepository, times(1)).save(any(ProductCategory.class));
    }

    /** Test validates correct exception class is thrown for duplicate parent category name */
    @Test
    void duplicate_name() {
        // Given
        var dto = new CategoryDTO(UUID.randomUUID().toString(), true, new HashSet<>());
        for (int i = 0; i < 50; i++) dto.getSub_category().add(new Faker().commerce().productName());

        var category = ProductCategory.builder()
                .categoryName(dto.getCategory_name().trim())
                .createAt(new Date())
                .isVisible(true)
                .productCategories(new HashSet<>())
                .product(new HashSet<>())
                .build();
        dto.getSub_category().forEach(str -> category.addCategory(new ProductCategory(str)));

        // When
        when(this.categoryRepository.duplicateCategoryName(dto.getCategory_name(), dto.getSub_category()))
                .thenReturn(5);

        // Then
        assertThrows(DuplicateException.class, () -> this.workerCategoryService.create(dto));
    }

    @Test
    void update() {
        // Given
        var dto = UpdateCategoryDTO.builder()
                .old_name(new Faker().commerce().productName())
                .new_name("Updated category name")
                .build();

        var category = ProductCategory.builder()
                .categoryName(dto.getOld_name())
                .createAt(new Date())
                .isVisible(true)
                .productCategories(new HashSet<>())
                .product(new HashSet<>())
                .build();

        // When
        when(this.categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));
        doReturn(0).when(this.categoryRepository).duplicateCategoryForUpdate(anyString());
        when(this.dateUTC.toUTC(any(Date.class))).thenReturn(Optional.of(new Date()));

        // Then
        assertEquals(OK, this.workerCategoryService.update(dto).getStatusCode());
        verify(this.categoryRepository, times(1))
                .update(any(Date.class), anyString(), anyString());
    }

    @Test
    void update_none_existing_category_name() {
        // Given
        var dto = UpdateCategoryDTO.builder()
                .old_name(new Faker().commerce().productName())
                .new_name(new Faker().commerce().productName())
                .build();

        // When
        when(this.categoryRepository.findByName(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(CustomNotFoundException.class, () -> this.workerCategoryService.update(dto));
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