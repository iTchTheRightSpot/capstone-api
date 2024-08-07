package dev.webserver.category;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.ProductResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}worker/category")
@RequiredArgsConstructor
class WorkerCategoryController {

    private final WorkerCategoryService service;

    @ResponseStatus(OK)
    @GetMapping(produces = "application/json")
    public List<Category> allCategories() {
        return service.allCategories();
    }

    @ResponseStatus(OK)
    @GetMapping(path = "/products", produces = "application/json")
    public Page<ProductResponse> allProductByCategory(
            @NotNull(message = "category_id cannot be null") @RequestParam(name = "category_id") Long id,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "currency", defaultValue = "ngn") String currency
    ) {
        final SarreCurrency s = SarreCurrency.valueOf(currency.toUpperCase());
        return service.allProductsByCategoryId(s, id, page, Math.min(size, 20));
    }

    @ResponseStatus(CREATED)
    @PostMapping(consumes = "application/json")
    public void create(@Valid @RequestBody CategoryDto dto) {
        service.create(dto);
    }

    @ResponseStatus(NO_CONTENT)
    @PutMapping(consumes = "application/json")
    public void update(@Valid @RequestBody UpdateCategoryDto dto) {
        service.update(dto);
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping(path = "/{category_id}")
    public void delete(@PathVariable(value = "category_id") Long id) {
        service.delete(id);
    }

}