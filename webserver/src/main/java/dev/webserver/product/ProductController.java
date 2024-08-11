package dev.webserver.product;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.util.Pageable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("${api.endpoint.baseurl}product")
@RequiredArgsConstructor
class ProductController {

    private final ProductService service;

    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Pageable<ProductResponse> allProducts(
            @RequestParam(name = "page", defaultValue = "0")
            final Integer page,
            @RequestParam(name = "size", defaultValue = "20")
            final Integer size,
            @RequestParam(name = "currency", defaultValue = "ngn")
            final String currency
    ) {
        final var sc = SarreCurrency.valueOf(currency.toUpperCase());
        return this.service.allProductsByCurrency(sc, page, Math.min(size, 20));
    }

    @ResponseStatus(OK)
    @GetMapping(path = "/find", produces = APPLICATION_JSON_VALUE)
    public Pageable<ProductResponse> search(
            @NotNull(message = "search param cannot be null")
            @NotEmpty(message = "search param cannot be empty")
            @RequestParam(name = "search")
            final String search,
            @NotNull @RequestParam(name = "size", defaultValue = "20")
            final Integer size,
            @RequestParam(name = "currency", defaultValue = "ngn")
            final String currency
    ) {
        final var c = SarreCurrency.valueOf(currency.toUpperCase());
        return this.service.search(search, c, size);
    }

    @ResponseStatus(OK)
    @GetMapping(path = "/detail", produces = APPLICATION_JSON_VALUE)
    public List<DetailResponse> productDetailsByProductUuid(
            @NotNull(message = "product_id cannot be null")
            @NotEmpty(message = "product_id cannot be empty")
            @RequestParam(value = "product_id")
            final String uuid,
            @RequestParam(value = "currency", defaultValue = "ngn")
            final String currency
    ) {
        final var c = SarreCurrency.valueOf(currency.toUpperCase());
        return this.service.productDetailsByProductUuid(uuid, c);
    }

}