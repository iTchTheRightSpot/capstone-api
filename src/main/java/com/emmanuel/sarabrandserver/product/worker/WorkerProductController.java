package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.product.util.CreateProductDTO;
import com.emmanuel.sarabrandserver.product.util.DetailDTO;
import com.emmanuel.sarabrandserver.product.util.ProductDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> fetchAll(
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "30") Integer size
    ) {
        return new ResponseEntity<>(this.workerProductService.fetchAll(page, Math.min(size, 30)), HttpStatus.OK);
    }

    /**
     * Method returns a list of DetailResponse.
     *
     * @param uuid is the Product UUID
     * @return ResponseEntity
     */
    @GetMapping(path = "/detail", produces = "application/json")
    public ResponseEntity<?> fetchAllProductDetails(@NotNull @RequestParam(value = "id") String uuid) {
        return new ResponseEntity<>(this.workerProductService.productDetailsByProductUUID(uuid), HttpStatus.OK);
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> create(@Valid @ModelAttribute CreateProductDTO dto) {
        return workerProductService.create(dto, dto.getFiles());
    }

    /**
     * Update a Product
     *
     * @param dto of type ProductDTO
     * @return ResponseEntity of type HttpStatus
     */
    @PutMapping(consumes = "application/json")
    public ResponseEntity<?> updateProduct(@Valid @RequestBody ProductDTO dto) {
        return this.workerProductService.updateProduct(dto);
    }

    /**
     * Update a ProductDetail
     *
     * @param dto of type DetailDTO
     * @return ResponseEntity of type HttpStatus
     */
    @PutMapping(path = "/detail", consumes = "application/json")
    public ResponseEntity<?> updateProductDetail(@Valid @RequestBody DetailDTO dto) {
        return this.workerProductService.updateProductDetail(dto);
    }

    /**
     * Method permanently deletes a Product
     *
     * @param uuid is the Product uuid
     * @return ResponseEntity of type HttpStatus
     */
    @DeleteMapping
    public ResponseEntity<?> deleteProduct(@NotNull @RequestParam(value = "id") String uuid) {
        return this.workerProductService.deleteProduct(uuid.trim());
    }

    /**
     * Method permanently deletes a ProductDetail
     *
     * @param sku is a unique String for each ProductDetail
     * @return ResponseEntity of type HttpStatus
     */
    @DeleteMapping(path = "/detail/{sku}")
    public ResponseEntity<?> deleteProductDetail(@NotNull @PathVariable(value = "sku") String sku) {
        return this.workerProductService.deleteProductDetail(sku);
    }

}
