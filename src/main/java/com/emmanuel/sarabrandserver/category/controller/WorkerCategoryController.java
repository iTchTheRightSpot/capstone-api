package com.emmanuel.sarabrandserver.category.controller;

import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/worker/category")
@PreAuthorize(value = "hasRole('ROLE_WORKER')")
public class WorkerCategoryController {
    private final WorkerCategoryService workerCategoryService;

    public WorkerCategoryController(WorkerCategoryService workerCategoryService) {
        this.workerCategoryService = workerCategoryService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> allCategories() {
        return new ResponseEntity<>(this.workerCategoryService.fetchAllCategories(), HttpStatus.OK);
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
