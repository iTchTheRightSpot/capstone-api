package com.sarabrandserver.cart.controller;

import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.response.CartResponse;
import com.sarabrandserver.cart.service.CartService;
import com.sarabrandserver.enumeration.SarreCurrency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "api/v1/client/cart")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ROLE_CLIENT')")
public class CartController {

    private final CartService cartService;

    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<CartResponse> cartItems(
            @RequestParam(name = "currency", defaultValue = "ngn") String currency
    ) {
        var c = SarreCurrency.valueOf(currency.toUpperCase());
        return this.cartService.cartItems(c);
    }

    @ResponseStatus(CREATED)
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public void create(@Valid @RequestBody CartDTO dto) {
        cartService.create(dto);
    }

    @ResponseStatus(OK)
    @DeleteMapping
    public void deleteItem(
            @NotNull @RequestParam(name = "session_id") Long id,
            @NotNull @RequestParam(name = "sku") String sku
    ) {
        this.cartService.remove_from_cart(id, sku);
    }

}
