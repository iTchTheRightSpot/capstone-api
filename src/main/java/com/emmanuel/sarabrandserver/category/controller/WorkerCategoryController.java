package com.emmanuel.sarabrandserver.category.controller;

import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.emmanuel.sarabrandserver.category.response.CategoryResponse;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.product.util.ProductResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "api/v1/worker/category")
@PreAuthorize(value = "hasRole('ROLE_WORKER')")
public class WorkerCategoryController {
    private final WorkerCategoryService workerCategoryService;

    public WorkerCategoryController(WorkerCategoryService workerCategoryService) {
        this.workerCategoryService = workerCategoryService;
    }

    @ResponseStatus(OK)
    @GetMapping(produces = "application/json")
    public List<CategoryResponse> allCategories() {
        return this.workerCategoryService.fetchAllCategories();
    }

    @ResponseStatus(OK)
    @GetMapping(path = "/products", produces = "application/json")
    public Page<ProductResponse> allProductByCategory(
            @NotNull @RequestParam(name = "id") String id,
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        return this.workerCategoryService
                .allProductsByCategory(id, page, Math.min(size, 20));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> create(@Valid @RequestBody CategoryDTO dto) {
        return this.workerCategoryService.create(dto);
    }

    @PutMapping(consumes = "application/json")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateCategoryDTO dto) {
        return this.workerCategoryService.update(dto);
    }

    @DeleteMapping(path = "/{name}")
    public ResponseEntity<?> delete(@PathVariable(value = "name") String name) {
        return this.workerCategoryService.delete(name);
    }

}
