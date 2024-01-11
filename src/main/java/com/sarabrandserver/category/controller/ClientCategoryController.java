package com.sarabrandserver.category.controller;

import com.sarabrandserver.category.response.CategoryResponse;
import com.sarabrandserver.category.service.ClientCategoryService;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.product.response.ProductResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}client/category")
@RequiredArgsConstructor
public class ClientCategoryController {

    private final ClientCategoryService clientCategoryService;

    /** Returns a list of parentId and child categories. */
    @ResponseStatus(OK)
    @GetMapping(produces = "application/json")
    public List<CategoryResponse> allCategories() {
        return this.clientCategoryService.allCategories();
    }

    /** Returns a list of ProductResponse objects based on categoryId uuid */
    @ResponseStatus(OK)
    @GetMapping(path = "/products", produces = "application/json")
    public Page<ProductResponse> fetchProductByCategory(
            @NotNull @RequestParam(name = "category_id") Long id,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "currency", defaultValue = "NGN") String currency
    ) {
        return this.clientCategoryService
                .allProductsByUUID(SarreCurrency.valueOf(currency), id, page, Math.min(size, 20));
    }

}
