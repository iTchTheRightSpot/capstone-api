<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/cart/controller/CartController.java
package dev.webserver.cart.controller;

import dev.webserver.cart.dto.CartDTO;
import dev.webserver.cart.response.CartResponse;
import dev.webserver.cart.service.CartService;
import dev.webserver.enumeration.SarreCurrency;
========
package dev.capstone.cart.controller;

import dev.capstone.cart.dto.CartDTO;
import dev.capstone.cart.response.CartResponse;
import dev.capstone.cart.service.CartService;
import dev.capstone.enumeration.SarreCurrency;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/cart/controller/CartController.java
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}cart")
@RequiredArgsConstructor
class CartController {

    private final CartService cartService;

    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<List<CartResponse>> cartItems(
            @RequestParam(name = "currency", defaultValue = "ngn") String currency,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        SarreCurrency s = SarreCurrency.valueOf(currency.toUpperCase());
        return this.cartService.cartItems(s, req, res);
    }

    @ResponseStatus(CREATED)
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public void create(@Valid @RequestBody CartDTO dto, HttpServletRequest req) {
        cartService.create(dto, req);
    }

    @ResponseStatus(OK)
    @DeleteMapping
    public void deleteItem(@NotNull @RequestParam(name = "sku") String sku, HttpServletRequest req) {
        this.cartService.deleteFromCart(req, sku);
    }

}