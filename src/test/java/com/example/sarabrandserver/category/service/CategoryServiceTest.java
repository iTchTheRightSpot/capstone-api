package com.example.sarabrandserver.category.service;

import com.example.sarabrandserver.category.dto.CategoryDTO;
import com.example.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.example.sarabrandserver.category.entity.ProductCategory;
import com.example.sarabrandserver.category.repository.CategoryRepository;
import com.example.sarabrandserver.category.response.CategoryResponse;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:application-dev.properties")
class CategoryServiceTest {

    private CategoryService categoryService;

    @Mock private CategoryRepository categoryRepository;

    @Mock private DateUTC dateUTC;

    @BeforeEach
    void setUp() {
        this.categoryService = new CategoryService(this.categoryRepository, this.dateUTC);
    }

    @Test
    void fetchAll() {
        // Given
        List<Object> parent = new ArrayList<>();
        parent.add(new Object[]{1L, new Faker().commerce().productName() + 1});
        parent.add(new Object[]{2L, new Faker().commerce().productName() + 2});

        List<String> child = new ArrayList<>();
        for (int i = 0; i < 5; i++) child.add(new Faker().animal().name());

        // When
        when(this.categoryRepository.getParentCategoriesWithIdNull()).thenReturn(parent);
        when(this.categoryRepository.getChildCategoriesWhereDeletedIsNull(anyLong())).thenReturn(child);

        // Then
        var req = this.categoryService.fetchAll();
        assertEquals(parent.size(), req.size());
        assertEquals(child.size(), req.get(0).sub_category().size());
        assertEquals(CategoryResponse.class, req.get(0).getClass());
        verify(this.categoryRepository, times(1)).getParentCategoriesWithIdNull();
        verify(this.categoryRepository, times(parent.size())).getChildCategoriesWhereDeletedIsNull(anyLong());
    }

    @Test
    void create() {
        // Given
        var dto = new CategoryDTO(new Faker().commerce().productName(), new ArrayList<>());
        for (int i = 0; i < 1; i++) dto.sub_category().add(new Faker().commerce().productName());

        var category = new ProductCategory(dto.category_name(), new Date());
        dto.sub_category().forEach(str -> category.addCategory(new ProductCategory(str)));

        // When
        when(this.categoryRepository.duplicateCategoryName(dto.category_name(), dto.sub_category()))
                .thenReturn(0);
        when(this.dateUTC.toUTC(any(Date.class))).thenReturn(Optional.of(new Date()));
        when(this.categoryRepository.save(any(ProductCategory.class))).thenReturn(category);

        // Then
        var res = this.categoryService.create(dto);
        assertEquals(CREATED, res.getStatusCode());
        verify(this.categoryRepository, times(1)).save(any(ProductCategory.class));
    }

    /** Test validates correct exception class is thrown for duplicate parent category name */
    @Test
    void duplicate_name() {
        // Given
        List<String> list = new ArrayList<>();
        var dto = new CategoryDTO(new Faker().commerce().productName(), new ArrayList<>());
        for (int i = 0; i < 1; i++) {
            dto.sub_category().add(new Faker().commerce().productName());
        }

        var category = new ProductCategory(dto.category_name(), new Date());
        dto.sub_category().forEach(str -> category.addCategory(new ProductCategory(str)));

        // When
        when(this.categoryRepository.duplicateCategoryName(dto.category_name(), dto.sub_category()))
                .thenReturn(1);

        // Then
        assertThrows(DuplicateException.class, () -> this.categoryService.create(dto));
    }

    @Test
    void update() {
        // Given
        var dto = new UpdateCategoryDTO(new Faker().commerce().productName(), new Faker().commerce().productName());
        var category = new ProductCategory(new Faker().commerce().productName(), new Date());

        // When
        when(this.categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));
        when(this.dateUTC.toUTC(any(Date.class))).thenReturn(Optional.of(new Date()));
        // Then
        var res = this.categoryService.update(dto);
        assertEquals(OK, res.getStatusCode());
        verify(this.categoryRepository, times(1))
                .update(any(Date.class), anyString(), anyString());
    }

    @Test
    void update_none_existing_category_name() {
        // Given
        var dto = new UpdateCategoryDTO(new Faker().commerce().productName(), new Faker().commerce().productName());

        // When
        when(this.categoryRepository.findByName(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(CustomNotFoundException.class, () -> this.categoryService.update(dto));
    }

    @Test
    void delete() {
        // Given
        var category = new ProductCategory(new Faker().commerce().productName(), new Date());

        // When
        Date date = new Date();
        when(this.dateUTC.toUTC(any(Date.class))).thenReturn(Optional.of(date));
        when(this.categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));

        // Then
        assertEquals(NO_CONTENT, this.categoryService.delete(category.getCategoryName()).getStatusCode());
        verify(this.categoryRepository, times(1))
                .custom_delete(date, category.getCategoryName());
    }

}