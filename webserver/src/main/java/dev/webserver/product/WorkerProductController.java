package dev.webserver.product;

import dev.webserver.enumeration.SarreCurrency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("${api.endpoint.baseurl}worker/product")
@RequiredArgsConstructor
class WorkerProductController {

    private final WorkerProductService service;

    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Page<ProductResponse> allProducts(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "currency", defaultValue = "ngn") String currency
    ) {
        SarreCurrency c = SarreCurrency.valueOf(currency.toUpperCase());
        return service.allProducts(c, page, Math.min(size, 20));
    }

    /**
     * Create a new {@link Product}
     */
    @ResponseStatus(CREATED)
    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public void create(
            @Valid @RequestPart CreateProductDto dto,
            @NotNull @RequestPart MultipartFile[] files
    ) {
        this.service.create(dto, files);
    }

    /**
     * Update a Product
     *
     * @param dto of type {@link UpdateProductDto}
     */
    @ResponseStatus(NO_CONTENT)
    @PutMapping(consumes = APPLICATION_JSON_VALUE)
    public void update(@Valid @RequestBody UpdateProductDto dto) {
        this.service.update(dto);
    }

    /**
     * Method permanently deletes a {@link Product}
     *
     * @param uuid is a unique string for every {@link Product} in the db.
     */
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping
    public void delete(@NotNull @RequestParam(value = "id") String uuid) {
        this.service.delete(uuid.trim());
    }

}