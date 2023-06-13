package com.example.sarabrandserver.product.service;

import com.example.sarabrandserver.category.service.CategoryService;
import com.example.sarabrandserver.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ClientProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

}
