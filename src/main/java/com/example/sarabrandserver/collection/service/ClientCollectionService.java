package com.example.sarabrandserver.collection.service;

import com.example.sarabrandserver.collection.repository.CollectionRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientCollectionService {

    private final CollectionRepository collectionRepository;

    public ClientCollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }
}
