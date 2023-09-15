package com.emmanuel.sarabrandserver.collection.controller;

import com.emmanuel.sarabrandserver.category.dto.UpdateCollectionDTO;
import com.emmanuel.sarabrandserver.collection.dto.CollectionDTO;
import com.emmanuel.sarabrandserver.collection.response.CollectionResponse;
import com.emmanuel.sarabrandserver.collection.service.WorkerCollectionService;
import com.emmanuel.sarabrandserver.product.util.ProductResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "api/v1/worker/collection")
@PreAuthorize(value = "hasRole('ROLE_WORKER')")
public class WorkerCollectionController {

    private final WorkerCollectionService collectionService;

    public WorkerCollectionController(WorkerCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @ResponseStatus(OK)
    @GetMapping(produces = "application/json")
    public List<CollectionResponse> fetchAll() {
        return this.collectionService.fetchAllCategories();
    }

    @ResponseStatus(OK)
    @GetMapping(path = "/products", produces = "application/json")
    public Page<ProductResponse> allProductByCollection(
            @NotNull @RequestParam(name = "id") String id,
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        return this.collectionService
                .allProductsByCollection(id, page, Math.min(size, 20));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> create(@Valid @RequestBody CollectionDTO dto) {
        return this.collectionService.create(dto);
    }

    @ResponseStatus(CREATED)
    @PutMapping(consumes = "application/json")
    public void update(@Valid @RequestBody UpdateCollectionDTO dto) {
        this.collectionService.update(dto);
    }

}
