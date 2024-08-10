package dev.webserver.category;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.ProductResponse;
import dev.webserver.util.Pageable;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}category")
@RequiredArgsConstructor
class ClientCategoryController {

    private final ClientCategoryService service;

    @ResponseStatus(OK)
    @GetMapping(produces = "application/json")
    public List<Category> allCategories() {
        return service.allCategories();
    }

    @ResponseStatus(OK)
    @GetMapping(path = "/products", produces = "application/json")
    public Pageable<ProductResponse> allProductsByCategoryId(
            @NotNull(message = "category_id cannot be null")
            @RequestParam(name = "category_id")
            final Long id,
            @RequestParam(name = "page", defaultValue = "0")
            final Integer page,
            @RequestParam(name = "size", defaultValue = "20")
            final Integer size,
            @RequestParam(name = "currency", defaultValue = "NGN")
            final String currency
    ) {
        return service.allProductsByCategoryId(SarreCurrency.valueOf(currency), id, page, Math.min(size, 20));
    }

}
