package com.sarabrandserver.cart.controller;

import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.response.CartResponse;
import com.sarabrandserver.cart.service.CartService;
import com.sarabrandserver.enumeration.SarreCurrency;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "api/v1/client/cart")
@RequiredArgsConstructor
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;

    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<CartResponse> cartItems(
            @RequestParam(name = "currency", defaultValue = "ngn") String currency,
            HttpServletRequest request
    ) {
        var c = SarreCurrency.valueOf(currency.toUpperCase());
        return this.cartService.cartItems(request.getRemoteAddr(), c);
    }

    @ResponseStatus(CREATED)
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public void create(@Valid @RequestBody CartDTO dto, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        log.info("My custom IpAddress {}", ip);
        cartService.create(ip, dto);
    }

    @ResponseStatus(OK)
    @DeleteMapping
    public void deleteItem(
            HttpServletRequest request,
            @NotNull @RequestParam(name = "sku") String sku
    ) {
        this.cartService.remove_from_cart(request.getRemoteAddr(), sku);
    }

}
