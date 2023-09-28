package com.emmanuel.sarabrandserver.product.controller;

import com.emmanuel.sarabrandserver.product.service.ClientProductService;
import com.emmanuel.sarabrandserver.product.util.ProductResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

/** All public routes */
@RestController
@RequestMapping("api/v1/client/product")
@RequiredArgsConstructor
public class ClientProductController {

    private final ClientProductService clientProductService;

    /** Returns a Page of ProductResponse objects */
    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Page<ProductResponse> allProducts(
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "40") Integer size
    ) {
        return this.clientProductService.allProductsByUUID("", "", page, Math.min(size, 40));
    }

    /** Returns a SseEmitter of a list of DetailResponse objects */
    @ResponseStatus(OK)
    @GetMapping(path = "/detail", produces = TEXT_EVENT_STREAM_VALUE)
    public SseEmitter productDetailsByProductUUID(@NotNull @RequestParam(value = "product_id") String uuid) {
        return this.clientProductService.productDetailsByProductUUID(uuid);
    }

}
