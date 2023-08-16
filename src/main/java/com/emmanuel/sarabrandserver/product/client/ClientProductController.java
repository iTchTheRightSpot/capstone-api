package com.emmanuel.sarabrandserver.product.client;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;

/** All public routes */
@RestController
@RequestMapping("api/v1/client/product")
public class ClientProductController {
    private final ClientProductService clientProductService;

    public ClientProductController(ClientProductService clientProductService) {
        this.clientProductService = clientProductService;
    }

    /** Returns a list of ProductResponse objects */
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> fetchAllProducts(
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "18") Integer size
    ) {
        return new ResponseEntity<>(this.clientProductService.fetchAll(page, size), OK);
    }

    /** Returns a list of DetailResponse objects */
    @GetMapping(path = "/{name}", produces = "application/json")
    public ResponseEntity<?> fetchProductDetails(@NotNull @PathVariable(value = "name") String name) {
        return new ResponseEntity<>(this.clientProductService.fetchAll(name), OK);
    }

}
