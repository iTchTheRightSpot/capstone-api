package com.example.sarabrandserver.collection.controller;

import com.example.sarabrandserver.collection.service.WorkerCollectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
