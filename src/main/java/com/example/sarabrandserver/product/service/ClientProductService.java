package com.example.sarabrandserver.product.service;

import com.example.sarabrandserver.product.projection.ClientProductPojo;
import com.example.sarabrandserver.product.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientProductService {
    private final ProductRepository productRepository;

    public ClientProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Returns a list of Products that are marked as visible. Also takes into consideration pagination where a max of
     * 30 items can be pulled as to not over load the Heap memory
     * @return List of ClientProductPojo
     * */
    public List<ClientProductPojo> fetchAll(int page, int size) {
        return this.productRepository.fetchAllClient(PageRequest.of(page, Math.min(size, 30)));
    }

}
