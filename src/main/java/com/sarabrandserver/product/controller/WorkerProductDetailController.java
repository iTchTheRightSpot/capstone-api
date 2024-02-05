package com.sarabrandserver.product.controller;

import com.sarabrandserver.product.service.ProductSkuService;
import com.sarabrandserver.product.service.WorkerProductDetailService;
import com.sarabrandserver.product.response.DetailResponse;
import com.sarabrandserver.product.dto.ProductDetailDTO;
import com.sarabrandserver.product.dto.UpdateProductDetailDTO;
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

    /**
     * Returns a list of DetailResponse.
     *
     * @param uuid is the Product UUID
     * @return List of DetailResponse
     */
    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<DetailResponse> get(@NotNull @RequestParam(value = "id") String uuid) {
        return this.detailService.productDetailsByProductUuid(uuid);
    }

    /**
     * Creates a new ProductDetail
     * */
    @ResponseStatus(CREATED)
    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public void create(
            @Valid @RequestPart ProductDetailDTO dto,
            @RequestParam(required = false) MultipartFile[] files
    ) {
        detailService.create(dto, files);
    }

    /**
     * Update a ProductDetail
     *
     * @param dto of type UpdateProductDetailDTO
     */
    @ResponseStatus(NO_CONTENT)
    @PutMapping(consumes = APPLICATION_JSON_VALUE)
    public void update(@Valid @RequestBody UpdateProductDetailDTO dto) {
        this.detailService.update(dto);
    }

    /**
     * Permanently deletes a ProductDetail
     *
     * @param sku is a unique String for each ProductSKU
     */
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping(path = "/{sku}")
    public void delete(@NotNull @PathVariable(value = "sku") String sku) {
        this.detailService.delete(sku);
    }

    /**
     * Permanently deletes a ProductSKU
     *
     * @param sku is a unique String for each ProductSKU
     */
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping(path = "/sku")
    public void deleteProductSKU(@NotNull @RequestParam(value = "sku") String sku) {
        this.skuService.delete(sku);
    }

}