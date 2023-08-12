package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.product.util.CreateProductDTO;
import com.emmanuel.sarabrandserver.product.util.DetailDTO;
import com.emmanuel.sarabrandserver.product.util.ProductDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * @param page is the UI page number
     * @param size is the amount in the list
     * @return ResponseEntity
     * */
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> fetchAll(
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "30") Integer size
    ) {
        return new ResponseEntity<>(this.workerProductService.fetchAll(page, Math.min(size, 30)), HttpStatus.OK);
    }

    /**
     * Method returns a list of DetailResponse.
     * @param name is the name of the product
     * @param page is amount of size based on the page
     * @param size is the amount in the list adn
     * @return ResponseEntity
     * */
    @GetMapping(path = "/{name}", produces = "application/json")
    public ResponseEntity<?> fetchAll(
            @NotNull @PathVariable(value = "name") String name,
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "15") Integer size
    ) {
        return new ResponseEntity<>(this.workerProductService.fetchAll(name, page, Math.min(size, 30)), HttpStatus.OK);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> create(
            @Valid @ModelAttribute CreateProductDTO dto,
            @RequestParam(name = "files") MultipartFile[] files
    ) {
        return workerProductService.create(dto, files);
    }

    /**
     * Update a Product
     * @param dto of type ProductDTO
     * @return ResponseEntity of type HttpStatus
     * */
    @PutMapping(consumes = "application/json")
    public ResponseEntity<?> updateProduct(@Valid @RequestBody ProductDTO dto) {
        return this.workerProductService.updateProduct(dto);
    }

    /**
     * Update a ProductDetail
     * @param dto of type DetailDTO
     * @return ResponseEntity of type HttpStatus
     * */
    @PutMapping(path = "/detail", consumes = "application/json")
    public ResponseEntity<HttpStatus> updateProductDetail(@Valid @RequestBody DetailDTO dto) {
        return this.workerProductService.updateProductDetail(dto);
    }

    /**
     * Method permanently deletes a Product
     * @param name is the Product name
     * @return ResponseEntity of type HttpStatus
     * */
    @DeleteMapping(path = "/{name}")
    public ResponseEntity<?> deleteProduct(@NotNull @PathVariable(value = "name") String name) {
        return this.workerProductService.deleteProduct(name);
    }

    /**
     * Method permanently deletes a ProductDetail
     * @param sku is a unique String for each ProductDetail
     * @return ResponseEntity of type HttpStatus
     * */
    @DeleteMapping(path = "/detail/{sku}")
    public ResponseEntity<?> deleteProductDetail(@NotNull @PathVariable(value = "sku") String sku) {
        return this.workerProductService.deleteProductDetail(sku);
    }

}
