package com.example.sarabrandserver.product.service;

import com.example.sarabrandserver.category.service.WorkerCategoryService;
import com.example.sarabrandserver.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientProductService {
    private final ProductRepository productRepository;
    private final WorkerCategoryService workerCategoryService;

    public ClientProductService(ProductRepository productRepository, WorkerCategoryService workerCategoryService) {
        this.productRepository = productRepository;
        this.workerCategoryService = workerCategoryService;
    }

}
