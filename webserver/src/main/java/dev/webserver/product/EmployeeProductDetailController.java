package dev.webserver.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("${api.endpoint.baseurl}employee/product/detail")
@RequiredArgsConstructor
class EmployeeProductDetailController {

    private final EmployeeProductDetailService detailService;
    private final ProductSkuService skuService;

    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<DetailResponse> productDetails(
            @NotNull(message = "product uuid cannot be null")
            @RequestParam(value = "id")
            final String uuid
    ) {
        return detailService.productDetailsByProductUuid(uuid);
    }

    @ResponseStatus(CREATED)
    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public void create(
            @Valid @RequestPart
            final ProductDetailDto dto,
            @RequestParam(required = false)
            final MultipartFile[] files
    ) {
        detailService.create(dto, files);
    }

    @ResponseStatus(NO_CONTENT)
    @PutMapping(consumes = APPLICATION_JSON_VALUE)
    public void update(@Valid @RequestBody final UpdateProductDetailDto dto) {
        detailService.update(dto);
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping(path = "/sku")
    public void deleteProductSku(@NotNull(message = "ProductSku sku cannot be null") @RequestParam(value = "sku") final String sku) {
        skuService.delete(sku);
    }

}