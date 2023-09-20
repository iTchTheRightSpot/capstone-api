package com.emmanuel.sarabrandserver.product.controller;

import com.emmanuel.sarabrandserver.product.util.CreateProductDTO;
import com.emmanuel.sarabrandserver.product.util.ProductResponse;
import com.emmanuel.sarabrandserver.product.util.UpdateProductDTO;
import com.emmanuel.sarabrandserver.product.service.WorkerProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("api/v1/worker/product")
@PreAuthorize(value = "hasRole('ROLE_WORKER')")
@RequiredArgsConstructor
public class WorkerProductController {

    private final WorkerProductService workerProductService;

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
     * Create a new Product
     */
    @ResponseStatus(CREATED)
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void createDetail(@Valid @ModelAttribute CreateProductDTO dto) {
        this.workerProductService.create(dto, dto.getFiles());
    }

    /**
     * Update a Product
     *
     * @param dto of type ProductDTO
     */
    @ResponseStatus(NO_CONTENT)
    @PutMapping(consumes = "application/json")
    public void updateProduct(@Valid @RequestBody UpdateProductDTO dto) {
        this.workerProductService.update(dto);
    }

    /**
     * Method permanently deletes a Product
     *
     * @param uuid is the Product uuid
     */
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping
    public void deleteProduct(@NotNull @RequestParam(value = "id") String uuid) {
        this.workerProductService.delete(uuid.trim());
    }

}
