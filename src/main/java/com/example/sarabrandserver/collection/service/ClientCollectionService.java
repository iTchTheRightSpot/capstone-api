package com.example.sarabrandserver.collection.service;

import com.example.sarabrandserver.collection.repository.ProductCollectionRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientCollectionService {

    private final ProductCollectionRepository productCollectionRepository;

    public ClientCollectionService(ProductCollectionRepository productCollectionRepository) {
        this.productCollectionRepository = productCollectionRepository;
    }
}
