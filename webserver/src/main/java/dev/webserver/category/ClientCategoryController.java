package dev.webserver.category;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.response.ProductResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}client/category")
@RequiredArgsConstructor
public class ClientCategoryController {

    private final ClientCategoryService service;

    @ResponseStatus(OK)
    @GetMapping(produces = "application/json")
    public List<CategoryResponse> allCategories() {
        return service.allCategories();
    }

    @ResponseStatus(OK)
    @GetMapping(path = "/products", produces = "application/json")
    public CompletableFuture<Page<ProductResponse>> allProductsByCategoryId(
            @NotNull @RequestParam(name = "category_id") Long id,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "currency", defaultValue = "NGN") String currency
    ) {
        return service
                .allProductsByCategoryId(SarreCurrency.valueOf(currency), id, page, Math.min(size, 20));
    }

}
