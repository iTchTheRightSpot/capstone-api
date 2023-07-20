package com.emmanuel.sarabrandserver.collection.service;

import com.emmanuel.sarabrandserver.collection.repository.CollectionRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientCollectionService {
    private final CollectionRepository collectionRepository;

    public ClientCollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }
}
