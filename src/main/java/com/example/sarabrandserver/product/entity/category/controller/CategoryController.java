package com.example.sarabrandserver.product.entity.category.controller;

import com.example.sarabrandserver.product.entity.category.dto.CategoryDTO;
import com.example.sarabrandserver.product.entity.category.dto.UpdateCategoryDTO;
import com.example.sarabrandserver.product.entity.category.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping(path = "api/v1/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> allCategories() {
        return new ResponseEntity<>(this.categoryService.fetchAll(), HttpStatus.OK);
    }

    @PostMapping(consumes = "application/json") @PreAuthorize(value = "hasAnyAuthority('WORKER')")
    public ResponseEntity<?> create(@Valid @RequestBody CategoryDTO dto) {
        return this.categoryService.create(dto);
    }

    @PutMapping(consumes = "application/json") @PreAuthorize(value = "hasAnyAuthority('WORKER')")
    public ResponseEntity<?> update(UpdateCategoryDTO dto) {
        return this.categoryService.update(dto);
    }

    @DeleteMapping(path = "/{category_id}") @PreAuthorize(value = "hasAnyAuthority('WORKER')")
    public ResponseEntity<?> delete(@PathVariable(value = "category_id") String id) {
        return this.categoryService.delete(id);
    }

}
