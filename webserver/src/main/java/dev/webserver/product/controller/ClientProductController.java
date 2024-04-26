package dev.webserver.product.controller;

import dev.webserver.aws.S3Service;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.repository.ProductRepo;
import dev.webserver.product.response.DetailResponse;
import dev.webserver.product.response.ProductResponse;
import dev.webserver.product.service.ClientProductService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("${api.endpoint.baseurl}client/product")
@RequiredArgsConstructor
public class ClientProductController {

    private final ClientProductService service;
//    private final ProductRepo repo;
//    private final S3Service s3Service;

    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<Page<ProductResponse>> allProducts(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "currency", defaultValue = "ngn") String currency
    ) {
        var sc = SarreCurrency.valueOf(currency.toUpperCase());
        return this.service.allProductsByCurrency(sc, page, Math.min(size, 20));
    }

//    @GetMapping("/test")
//    public String test () {
//        var prod = repo.findAll().getFirst();
//        System.out.println("product " + prod.toString());
//        return s3Service.preSignedUrl("my-vps-bucket", prod.getDefaultKey());
//    }

    @ResponseStatus(OK)
    @GetMapping(path = "/find", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<Page<ProductResponse>> search(
            @NotNull @RequestParam(name = "search") String search,
            @NotNull @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "currency", defaultValue = "ngn") String currency
    ) {
        var c = SarreCurrency.valueOf(currency.toUpperCase());
        return this.service.search(search, c, size);
    }

    @ResponseStatus(OK)
    @GetMapping(path = "/detail", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<List<DetailResponse>> productDetailsByProductUuid(
            @NotNull @RequestParam(value = "product_id") String uuid,
            @RequestParam(value = "currency", defaultValue = "ngn") String currency
    ) {
        var c = SarreCurrency.valueOf(currency.toUpperCase());
        return this.service.productDetailsByProductUuid(uuid, c);
    }

}