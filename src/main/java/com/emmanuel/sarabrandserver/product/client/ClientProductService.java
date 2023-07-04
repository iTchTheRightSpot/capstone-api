package com.emmanuel.sarabrandserver.product.client;

import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.response.DetailResponse;
import com.emmanuel.sarabrandserver.product.response.ProductResponse;
import org.springframework.data.domain.Page;
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
     * Method list all Products when a client clicks on shop all. Query method fetchAllProductsClient validates product
     * visibility property is true and inventory is greater than 0.
     * @param page is the number to display a products
     * @param size is the max amount render on a page
     * @return List of ProductResponse
     * */
    public Page<ProductResponse> fetchAll(int page, int size) {
        return this.productRepository
                .fetchAllProductsClient(PageRequest.of(page, Math.max(size, 30))) //
                .map(pojo -> new ProductResponse(
                        pojo.getName(),
                        pojo.getDesc(),
                        pojo.getPrice().doubleValue(),
                        pojo.getCurrency(),
                        pojo.getKey()
                ));
    }

    /**
     * Method list all ProductDetails bases on a Product name. A validation is made to make sure product visibility is
     * true and inventory count is greater than zero.
     * @param name is the name of the product
     * @return List of DetailResponse
     * */
    public List<DetailResponse> fetchAll(String name) {
        return this.productRepository.fetchDetailClient(name) //
                .stream() //
                .map(p -> new DetailResponse(p.getSize(), p.getColour(), p.getKey())) //
                .toList();
    }

    /**
     * Method list all products based on category clicked. It filters validating visibility property is true and
     * inventory is greater than 0.
     * @param name is category name
     * @param page is the number to display a products
     * @param size is the max amount render on a page
     * @return List of ProductResponse
     * */
    public List<ProductResponse> fetchAll(String name, int page, int size) {
        return this.productRepository.fetchByCategoryClient(name, PageRequest.of(page, Math.max(size, 30))) //
                .stream() //
                .map(pojo -> new ProductResponse(
                        pojo.getName(),
                        pojo.getDesc(),
                        pojo.getPrice().doubleValue(),
                        pojo.getCurrency(),
                        pojo.getKey()
                )) //
                .toList();
    }

}

