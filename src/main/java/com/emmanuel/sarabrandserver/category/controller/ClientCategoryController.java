package com.emmanuel.sarabrandserver.category.controller;

import com.emmanuel.sarabrandserver.category.service.ClientCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController @RequestMapping(path = "api/v1/client/category")
public class ClientCategoryController {

    private final ClientCategoryService clientCategoryService;

    public ClientCategoryController(ClientCategoryService clientCategoryService) {
        this.clientCategoryService = clientCategoryService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> allCategories() {
        return new ResponseEntity<>(this.clientCategoryService.fetchAll(), OK);
    }

    @GetMapping(path = "/product", produces = "application/json")
    public ResponseEntity<?> fetchProductByCategory(
            @RequestParam(value = "name") String name,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        return new ResponseEntity<>(this.clientCategoryService.fetchProductOnCategory(name, page, size), OK);
    }

}
