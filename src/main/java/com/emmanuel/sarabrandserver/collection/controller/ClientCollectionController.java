package com.emmanuel.sarabrandserver.collection.controller;

import com.emmanuel.sarabrandserver.collection.service.ClientCollectionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping(path = "api/v1/client/collection")
public class ClientCollectionController {
    private final ClientCollectionService collectionService;

    public ClientCollectionController(ClientCollectionService collectionService) {
        this.collectionService = collectionService;
    }
    
}
