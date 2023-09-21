package com.emmanuel.sarabrandserver.product.controller;

import com.emmanuel.sarabrandserver.product.service.ProductSKUService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("api/v1/worker/product/detail/sku")
@PreAuthorize(value = "hasRole('ROLE_WORKER')")
@RequiredArgsConstructor
public class ProductSKUController {

    private final ProductSKUService productSKUService;

    /**
     * Method permanently deletes a ProductDetail
     *
     * @param sku is a unique String for each ProductDetail
     */
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping
    public void deleteProductSKU(@NotNull @RequestParam(value = "sku") String sku) {
        this.productSKUService.deleteProductSKU(sku);
    }

}
