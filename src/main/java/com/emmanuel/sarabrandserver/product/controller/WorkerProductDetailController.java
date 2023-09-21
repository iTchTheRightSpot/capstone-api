package com.emmanuel.sarabrandserver.product.controller;

import com.emmanuel.sarabrandserver.product.service.WorkerProductDetailService;
import com.emmanuel.sarabrandserver.product.util.DetailResponse;
import com.emmanuel.sarabrandserver.product.util.ProductDetailDTO;
import com.emmanuel.sarabrandserver.product.util.UpdateProductDetailDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("api/v1/worker/product/detail")
@PreAuthorize(value = "hasRole('ROLE_WORKER')")
@RequiredArgsConstructor
public class WorkerProductDetailController {

    private final WorkerProductDetailService workerProductDetailService;

    /**
     * Method returns a list of DetailResponse.
     *
     * @param uuid is the Product UUID
     * @return ResponseEntity
     */
    @ResponseStatus(OK)
    @GetMapping(produces = "application/json")
    public List<DetailResponse> fetchAllProductDetails(@NotNull @RequestParam(value = "id") String uuid) {
        return this.workerProductDetailService.fetch(uuid);
    }

    @ResponseStatus(CREATED)
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void createDetail(@Valid @ModelAttribute ProductDetailDTO dto) {
        workerProductDetailService.create(dto, dto.getFiles());
    }

    /**
     * Update a ProductDetail
     *
     * @param dto of type DetailDTO
     */
    @ResponseStatus(NO_CONTENT)
    @PutMapping(consumes = "application/json")
    public void updateProductDetail(@Valid @RequestBody UpdateProductDetailDTO dto) {
        this.workerProductDetailService.update(dto);
    }

    /**
     * Method permanently deletes a ProductDetail
     *
     * @param sku is a unique String for each ProductDetail
     */
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping(path = "/{sku}")
    public void deleteProductDetail(@NotNull @PathVariable(value = "sku") String sku) {
        this.workerProductDetailService.delete(sku);
    }

}