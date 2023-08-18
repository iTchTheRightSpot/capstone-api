package com.emmanuel.sarabrandserver.collection.controller;

import com.emmanuel.sarabrandserver.collection.service.ClientCollectionService;
import com.emmanuel.sarabrandserver.product.client.ClientProductService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController @RequestMapping(path = "api/v1/client/collection")
public class ClientCollectionController {
    private final ClientCollectionService collectionService;
    private final ClientProductService productService;

    public ClientCollectionController(ClientCollectionService collectionService, ClientProductService productService) {
        this.collectionService = collectionService;
        this.productService = productService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> allCollections() {
        return new ResponseEntity<>(this.collectionService.fetchAll(), OK);
    }

    /** Returns a list of ProductResponse objects based on category name */
    @GetMapping(path = "/product", produces = "application/json")
    public ResponseEntity<?> fetchProductByCollection(
            @NotNull @RequestParam(name = "name") String name,
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "18") Integer size
    ) {
        return new ResponseEntity<>(this.productService.fetchProductOnCollection(name, page, size), OK);
    }
    
}
