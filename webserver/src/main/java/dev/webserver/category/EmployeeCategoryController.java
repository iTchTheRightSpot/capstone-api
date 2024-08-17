package dev.webserver.category;

import dev.webserver.enumeration.CapstoneCurrency;
import dev.webserver.product.ProductResponse;
import dev.webserver.util.Pageable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}employee/category")
@RequiredArgsConstructor
class EmployeeCategoryController {

    private final EmployeeCategoryService service;

    @ResponseStatus(OK)
    @GetMapping(produces = "application/json")
    public List<CategoryResponse> allCategories() {
        return service.allCategories();
    }

    @ResponseStatus(OK)
    @GetMapping(path = "/products", produces = "application/json")
    public Pageable<ProductResponse> allProductByCategory(
            @NotNull(message = "category_id cannot be null")
            @RequestParam(name = "category_id")
            final Long id,
            @RequestParam(name = "page", defaultValue = "0")
            final Integer page,
            @RequestParam(name = "size", defaultValue = "20")
            final Integer size,
            @RequestParam(name = "currency", defaultValue = "ngn")
            final String currency
    ) {
        final CapstoneCurrency s = CapstoneCurrency.valueOf(currency.toUpperCase());
        return service.allProductsByCategoryId(s, id, page, Math.min(size, 20));
    }

    @ResponseStatus(CREATED)
    @PostMapping(consumes = "application/json")
    public void create(@Valid @RequestBody final CategoryDto dto) {
        service.create(dto);
    }

    @ResponseStatus(NO_CONTENT)
    @PutMapping(consumes = "application/json")
    public void update(@Valid @RequestBody final UpdateCategoryDto dto) {
        service.update(dto);
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping(path = "/{category_id}")
    public void delete(@PathVariable(value = "category_id") final Long id) {
        service.delete(id);
    }

}