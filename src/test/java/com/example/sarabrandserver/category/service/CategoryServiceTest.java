package com.example.sarabrandserver.category.service;

import com.example.sarabrandserver.category.dto.CategoryDTO;
import com.example.sarabrandserver.category.entity.ProductCategory;
import com.example.sarabrandserver.category.repository.CategoryRepository;
import com.example.sarabrandserver.category.response.CategoryResponse;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:application-dev.properties")
class CategoryServiceTest {

    private CategoryService categoryService;

    @Mock private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        this.categoryService = new CategoryService(this.categoryRepository);
    }

    @Test
    void fetchAll() {
        // Given
        var one = new ProductCategory(new Faker().commerce().productName());
        var two = new ProductCategory(new Faker().commerce().department());
        var three = new ProductCategory(new Faker().commerce().color());

        for (int i = 0; i < 50; i++) {
            one.addCategory(new ProductCategory(new Faker().commerce().productName()));
            two.addCategory(new ProductCategory(new Faker().commerce().productName()));
            three.addCategory(new ProductCategory(new Faker().commerce().productName()));
        }

        // When
        when(this.categoryRepository.allCategoryName()).thenReturn(List.of(one, two, three));

        // Then
        var req = this.categoryService.fetchAll();
        assertEquals(3, req.size());
        assertEquals(CategoryResponse.class, req.get(0).getClass());
    }

    @Test
    void create() {
        // Given
        List<String> list = new ArrayList<>();
        var dto = new CategoryDTO(new Faker().commerce().productName(), list);
        for (int i = 0; i < 50; i++) {
            list.add(new Faker().commerce().productName());
        }

        var category = new ProductCategory(dto.category_name());
        dto.sub_category().forEach(str -> category.addCategory(new ProductCategory(str)));

        // When
        when(this.categoryRepository.duplicateCategoryName(dto.category_name(), dto.sub_category())).thenReturn(0);
        when(this.categoryRepository.save(any(ProductCategory.class))).thenReturn(category);

        // Then
        var res = this.categoryService.create(dto);
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        verify(this.categoryRepository, times(1)).save(any(ProductCategory.class));
    }

    @Test
    void update() {}

    @Test
    void delete() {}

}