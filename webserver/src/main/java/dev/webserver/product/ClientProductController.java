package dev.webserver.product;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.util.Pageable;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("${api.endpoint.baseurl}product")
@RequiredArgsConstructor
class ClientProductController {

    private final ClientProductService service;

    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Pageable<ProductResponse> allProducts(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "currency", defaultValue = "ngn") String currency
    ) {
        var sc = SarreCurrency.valueOf(currency.toUpperCase());
        return this.service.allProductsByCurrency(sc, page, Math.min(size, 20));
    }

    @ResponseStatus(OK)
    @GetMapping(path = "/find", produces = APPLICATION_JSON_VALUE)
    public Pageable<ProductResponse> search(
            @NotNull @RequestParam(name = "search") String search,
            @NotNull @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "currency", defaultValue = "ngn") String currency
    ) {
        var c = SarreCurrency.valueOf(currency.toUpperCase());
        return this.service.search(search, c, size);
    }

    @ResponseStatus(OK)
    @GetMapping(path = "/detail", produces = APPLICATION_JSON_VALUE)
    public List<DetailResponse> productDetailsByProductUuid(
            @NotNull @RequestParam(value = "product_id") String uuid,
            @RequestParam(value = "currency", defaultValue = "ngn") String currency
    ) {
        var c = SarreCurrency.valueOf(currency.toUpperCase());
        return this.service.productDetailsByProductUuid(uuid, c);
    }

}