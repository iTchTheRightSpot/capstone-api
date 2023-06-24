package com.emmanuel.sarabrandserver.collection.controller;

import com.emmanuel.sarabrandserver.collection.dto.CollectionDTO;
import com.emmanuel.sarabrandserver.collection.service.WorkerCollectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping(path = "api/v1/worker/collection")
//@PreAuthorize(value = "hasAnyAuthority('WORKER')")
public class WorkerCollectionController {
    private final WorkerCollectionService collectionService;

    public WorkerCollectionController(WorkerCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> fetchAll() {
        return new ResponseEntity<>(this.collectionService.fetchAll(), HttpStatus.OK);
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> create(@Valid @RequestBody CollectionDTO dto) {
        return this.collectionService.create(dto);
    }

}
