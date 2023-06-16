package com.example.sarabrandserver.product.controller;

import com.example.sarabrandserver.product.dto.CreateProductDTO;
import com.example.sarabrandserver.product.dto.UpdateProductDTO;
import com.example.sarabrandserver.product.service.WorkerProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/worker/product")
@PreAuthorize(value = "hasAnyAuthority('WORKER')")
public class WorkerProductController {

    private final WorkerProductService workerProductService;

    public WorkerProductController(WorkerProductService workerProductService) {
        this.workerProductService = workerProductService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> fetchAll(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        return new ResponseEntity<>(this.workerProductService.fetchAll(page, size), HttpStatus.OK);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> create(
            @Valid @ModelAttribute CreateProductDTO dto,
            @RequestParam(name = "file") MultipartFile file
    ) {
        return workerProductService.create(dto, file);
    }

    @PutMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> update(
            @Valid @ModelAttribute UpdateProductDTO dto,
            @RequestParam(name = "file", required = false) MultipartFile file
    ) {
        return this.workerProductService.updateProduct(dto, file);
    }

    /**
     * Method permanently deletes all Product with param
     * @param name is the Product name
     * @return ResponseEntity of type HttpStatus (204).
     * */
    @DeleteMapping
    public ResponseEntity<?> deleteAllProduct(@RequestParam(name = "name") String name) {
        return this.workerProductService.deleteAllProduct(name);
    }

    /**
     * Method permanently deletes a Product
     * @param name is the Product name
     * @param sku is a unique String for each ProductDetail
     * @return ResponseEntity of type String
     * */
    @DeleteMapping(path = "/{name}/{sku}")
    public ResponseEntity<?> deleteAProduct(
       @PathVariable(value = "name") String name,
       @PathVariable(value = "sku") String sku
    ) {
        return this.workerProductService.deleteAProduct(name, sku);
    }

}
