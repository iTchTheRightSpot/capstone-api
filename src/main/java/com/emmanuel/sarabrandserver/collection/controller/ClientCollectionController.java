package com.emmanuel.sarabrandserver.collection.controller;

import com.emmanuel.sarabrandserver.collection.response.CollectionResponse;
import com.emmanuel.sarabrandserver.collection.service.ClientCollectionService;
import com.emmanuel.sarabrandserver.product.service.ClientProductService;
import com.emmanuel.sarabrandserver.product.util.ProductResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "api/v1/client/collection")
@RequiredArgsConstructor
public class ClientCollectionController {

    private final ClientCollectionService collectionService;
    private final ClientProductService productService;

    @ResponseStatus(OK)
    @GetMapping(produces = "application/json")
    public List<CollectionResponse> allCollections() {
        return this.collectionService.fetchAll();
    }

    /** Returns a list of ProductResponse objects based on collection id */
    @ResponseStatus(OK)
    @GetMapping(path = "/products", produces = "application/json")
    public Page<ProductResponse> fetchProductByCollection(
            @NotNull @RequestParam(name = "collection_id") String uuid,
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        return this.productService.allProductsByUUID("collection", uuid, page, Math.min(size, 20));
    }
    
}
