package com.example.sarabrandserver.collection.service;

import com.example.sarabrandserver.collection.repository.ProductCollectionRepository;
import org.springframework.stereotype.Service;

@Service
public class WorkerCollectionService {

    private final ProductCollectionRepository productCollectionRepository;

    public WorkerCollectionService(ProductCollectionRepository productCollectionRepository) {
        this.productCollectionRepository = productCollectionRepository;
    }

}
