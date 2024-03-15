package com.sarabrandserver.category.controller;

import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.sarabrandserver.category.response.WorkerCategoryResponse;
import com.sarabrandserver.category.service.WorkerCategoryService;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.product.response.ProductResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}worker/category")
@PreAuthorize(value = "hasRole('ROLE_WORKER')")
@RequiredArgsConstructor
public class WorkerCategoryController {

    private final WorkerCategoryService workerCategoryService;

    @ResponseStatus(OK)
    @GetMapping(produces = "application/json")
    public WorkerCategoryResponse allCategories() {
        return this.workerCategoryService.allCategories();
    }

    @ResponseStatus(OK)
    @GetMapping(path = "/products", produces = "application/json")
    public CompletableFuture<Page<ProductResponse>> allProductByCategory(
            @NotNull @RequestParam(name = "category_id") Long id,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "currency", defaultValue = "ngn") String currency
    ) {
        var s =  SarreCurrency.valueOf(currency.toUpperCase());
        return this.workerCategoryService
                .allProductsByCategoryId(s, id, page, Math.min(size, 20));
    }

    @ResponseStatus(CREATED)
    @PostMapping(consumes = "application/json")
    public void create(@Valid @RequestBody CategoryDTO dto) {
        this.workerCategoryService.create(dto);
    }

    @ResponseStatus(NO_CONTENT)
    @PutMapping(consumes = "application/json")
    public void update(@Valid @RequestBody UpdateCategoryDTO dto) {
        this.workerCategoryService.update(dto);
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable(value = "id") Long id) {
        this.workerCategoryService.delete(id);
    }

}