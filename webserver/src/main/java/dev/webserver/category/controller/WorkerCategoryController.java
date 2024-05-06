package dev.webserver.category.controller;

import dev.webserver.category.dto.CategoryDTO;
import dev.webserver.category.dto.UpdateCategoryDTO;
import dev.webserver.category.response.WorkerCategoryResponse;
import dev.webserver.category.service.WorkerCategoryService;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.response.ProductResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}worker/category")
@RequiredArgsConstructor
public class WorkerCategoryController {

    private final WorkerCategoryService service;

    @ResponseStatus(OK)
    @GetMapping(produces = "application/json")
    public WorkerCategoryResponse allCategories() {
        return this.service.allCategories();
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
        return this.service
                .allProductsByCategoryId(s, id, page, Math.min(size, 20));
    }

    @ResponseStatus(CREATED)
    @PostMapping(consumes = "application/json")
    public void create(@Valid @RequestBody CategoryDTO dto) {
        this.service.create(dto);
    }

    @ResponseStatus(NO_CONTENT)
    @PutMapping(consumes = "application/json")
    public void update(@Valid @RequestBody UpdateCategoryDTO dto) {
        this.service.update(dto);
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable(value = "id") Long id) {
        this.service.delete(id);
    }

}