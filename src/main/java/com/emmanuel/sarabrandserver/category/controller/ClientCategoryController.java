package com.emmanuel.sarabrandserver.category.controller;

import com.emmanuel.sarabrandserver.category.response.CategoryResponse;
import com.emmanuel.sarabrandserver.category.service.ClientCategoryService;
import com.emmanuel.sarabrandserver.product.service.ClientProductService;
import com.emmanuel.sarabrandserver.product.util.ProductResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "api/v1/client/category")
@RequiredArgsConstructor
public class ClientCategoryController {

    private final ClientCategoryService clientCategoryService;
    private final ClientProductService productService;

    /** Returns a list of parent and child categories. */
    @ResponseStatus(OK)
    @GetMapping(produces = "application/json")
    public List<CategoryResponse> allCategories() {
        return this.clientCategoryService.fetchAll();
    }

    /** Returns a list of ProductResponse objects based on category uuid */
    @ResponseStatus(OK)
    @GetMapping(path = "/products", produces = "application/json")
    public Page<ProductResponse> fetchProductByCategory(
            @NotNull @RequestParam(name = "category_id") String id,
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        return this.productService.allProductsByUUID("category", id, page, Math.min(size, 20));
    }

}
