package com.emmanuel.sarabrandserver.product.controller;

import com.emmanuel.sarabrandserver.product.service.ClientProductService;
import com.emmanuel.sarabrandserver.product.util.DetailResponse;
import com.emmanuel.sarabrandserver.product.util.ProductResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

/** All public routes */
@RestController
@RequestMapping("api/v1/client/product")
@RequiredArgsConstructor
public class ClientProductController {

    private final ClientProductService clientProductService;

    /** Returns a list of ProductResponse objects */
    @ResponseStatus(OK)
    @GetMapping(produces = "application/json")
    public Page<ProductResponse> fetchAllProducts(
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "40") Integer size
    ) {
        return this.clientProductService.fetchAllByUUID("", "", page, size);
    }

    /** Returns a list of DetailResponse objects */
    @ResponseStatus(OK)
    @GetMapping(path = "/detail", produces = "application/json")
    public List<DetailResponse> fetchProductDetails(@NotNull @RequestParam(value = "id") String uuid) {
        return this.clientProductService.productDetailByUUID(uuid);
    }

}
