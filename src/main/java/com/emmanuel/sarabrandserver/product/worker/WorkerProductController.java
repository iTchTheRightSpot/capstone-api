package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.product.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("api/v1/worker/product")
@PreAuthorize(value = "hasRole('ROLE_WORKER')")
public class WorkerProductController {
    private final WorkerProductService workerProductService;

    public WorkerProductController(WorkerProductService workerProductService) {
        this.workerProductService = workerProductService;
    }

    /**
     * Method fetches a list of ProductResponse.
     *
     * @param page is the UI page number
     * @param size is the amount in the list
     * @return ResponseEntity
     */
    @ResponseStatus(OK)
    @GetMapping(produces = "application/json")
    public Page<ProductResponse> fetchAll(
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "30") Integer size
    ) {
        return this.workerProductService.fetchAll(page, Math.min(size, 30));
    }

    /**
     * Method returns a list of DetailResponse.
     *
     * @param uuid is the Product UUID
     * @return ResponseEntity
     */
    @ResponseStatus(OK)
    @GetMapping(path = "/detail", produces = "application/json")
    public List<DetailResponse> fetchAllProductDetails(@NotNull @RequestParam(value = "id") String uuid) {
        return this.workerProductService.productDetailsByProductUUID(uuid);
    }

    @ResponseStatus(CREATED)
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void create(@Valid @ModelAttribute CreateProductDTO dto) {
        workerProductService.create(dto, dto.getFiles());
    }

    /**
     * Update a Product
     *
     * @param dto of type ProductDTO
     */
    @ResponseStatus(NO_CONTENT)
    @PutMapping(consumes = "application/json")
    public void updateProduct(@Valid @RequestBody UpdateProductDTO dto) {
        this.workerProductService.updateProduct(dto);
    }

    /**
     * Update a ProductDetail
     *
     * @param dto of type DetailDTO
     */
    @ResponseStatus(NO_CONTENT)
    @PutMapping(path = "/detail", consumes = "application/json")
    public void updateProductDetail(@Valid @RequestBody DetailDTO dto) {
        this.workerProductService.updateProductDetail(dto);
    }

    /**
     * Method permanently deletes a Product
     *
     * @param uuid is the Product uuid
     */
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping
    public void deleteProduct(@NotNull @RequestParam(value = "id") String uuid) {
        this.workerProductService.deleteProduct(uuid.trim());
    }

    /**
     * Method permanently deletes a ProductDetail
     *
     * @param sku is a unique String for each ProductDetail
     */
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping(path = "/detail/{sku}")
    public void deleteProductDetail(@NotNull @PathVariable(value = "sku") String sku) {
        this.workerProductService.deleteProductDetail(sku);
    }

}
