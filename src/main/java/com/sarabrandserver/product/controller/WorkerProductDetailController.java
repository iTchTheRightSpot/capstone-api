package com.sarabrandserver.product.controller;

import com.sarabrandserver.product.service.ProductSkuService;
import com.sarabrandserver.product.service.WorkerProductDetailService;
import com.sarabrandserver.product.response.DetailResponse;
import com.sarabrandserver.product.dto.ProductDetailDto;
import com.sarabrandserver.product.dto.UpdateProductDetailDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("${api.endpoint.baseurl}worker/product/detail")
@PreAuthorize(value = "hasRole('ROLE_WORKER')")
@RequiredArgsConstructor
public class WorkerProductDetailController {

    private final WorkerProductDetailService detailService;
    private final ProductSkuService skuService;

    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<DetailResponse> productDetails(
            @NotNull @RequestParam(value = "id") String uuid
    ) {
        return this.detailService.productDetailsByProductUuid(uuid);
    }

    @ResponseStatus(CREATED)
    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public void create(
            @Valid @RequestPart ProductDetailDto dto,
            @RequestParam(required = false) MultipartFile[] files
    ) {
        detailService.create(dto, files);
    }

    @ResponseStatus(NO_CONTENT)
    @PutMapping(consumes = APPLICATION_JSON_VALUE)
    public void update(@Valid @RequestBody UpdateProductDetailDto dto) {
        this.detailService.update(dto);
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping(path = "/{sku}")
    public void delete(@NotNull @PathVariable(value = "sku") String sku) {
        this.detailService.delete(sku);
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping(path = "/sku")
    public void deleteProductSKU(@NotNull @RequestParam(value = "sku") String sku) {
        this.skuService.delete(sku);
    }

}