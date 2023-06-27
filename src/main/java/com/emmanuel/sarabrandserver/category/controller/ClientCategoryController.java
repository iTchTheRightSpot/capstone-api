package com.emmanuel.sarabrandserver.category.controller;

import com.emmanuel.sarabrandserver.category.service.ClientCategoryService;
import com.emmanuel.sarabrandserver.product.client.ClientProductService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController @RequestMapping(path = "api/v1/client/category")
public class ClientCategoryController {
    private final ClientCategoryService clientCategoryService;
    private final ClientProductService productService;

    public ClientCategoryController(
            ClientCategoryService clientCategoryService,
            ClientProductService productService
    ) {
        this.clientCategoryService = clientCategoryService;
        this.productService = productService;
    }

    /** Returns a list of parent and child categories. */
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> allCategories() {
        return new ResponseEntity<>(this.clientCategoryService.fetchAll(), OK);
    }

    /** Returns a list of ProductResponse objects based on category name */
    @GetMapping(path = "/product", produces = "application/json")
    public ResponseEntity<?> fetchProductByCategory(
            @NotNull @RequestParam(name = "name") String name,
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        return new ResponseEntity<>(this.productService.fetchAll(name, page, size), OK);
    }

}
