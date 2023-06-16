package com.example.sarabrandserver.category.controller;

import com.example.sarabrandserver.category.service.ClientCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping(path = "api/v1/client/category")
public class ClientCategoryController {

    private final ClientCategoryService clientCategoryService;

    public ClientCategoryController(ClientCategoryService clientCategoryService) {
        this.clientCategoryService = clientCategoryService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> allCategories() {
        return new ResponseEntity<>(this.clientCategoryService.fetchAll(), HttpStatus.OK);
    }

}
