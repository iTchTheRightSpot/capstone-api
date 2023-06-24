package com.emmanuel.sarabrandserver.product.controller;

import com.emmanuel.sarabrandserver.product.service.ClientProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

/** All public routes */
@RestController
@RequestMapping("api/v1/client/product")
public class ClientProductController {

    private final ClientProductService clientProductService;

    public ClientProductController(ClientProductService clientProductService) {
        this.clientProductService = clientProductService;
    }

    public ResponseEntity<?> fetchAll(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        return new ResponseEntity<>(this.clientProductService.fetchAll(page, size), OK);
    }

}
