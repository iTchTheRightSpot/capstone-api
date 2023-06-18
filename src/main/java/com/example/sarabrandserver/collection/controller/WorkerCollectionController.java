package com.example.sarabrandserver.collection.controller;

import com.example.sarabrandserver.collection.service.WorkerCollectionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping(path = "api/v1/client/collection")
public class WorkerCollectionController {

    private final WorkerCollectionService collectionService;

    public WorkerCollectionController(WorkerCollectionService collectionService) {
        this.collectionService = collectionService;
    }

}
