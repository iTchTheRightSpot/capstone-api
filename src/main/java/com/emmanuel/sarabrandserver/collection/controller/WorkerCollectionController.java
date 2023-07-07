package com.emmanuel.sarabrandserver.collection.controller;

import com.emmanuel.sarabrandserver.collection.dto.CollectionDTO;
import com.emmanuel.sarabrandserver.collection.service.WorkerCollectionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping(path = "api/v1/worker/collection")
@PreAuthorize(value = "hasRole('ROLE_WORKER')")
public class WorkerCollectionController {
    private final WorkerCollectionService collectionService;

    public WorkerCollectionController(WorkerCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> fetchAll(
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "30") Integer size
    ) {
        return new ResponseEntity<>(this.collectionService.fetchAll(page, size), HttpStatus.OK);
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> create(@Valid @RequestBody CollectionDTO dto) {
        return this.collectionService.create(dto);
    }

}
