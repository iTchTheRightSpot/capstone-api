package com.sarabrandserver.cart.service;

import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.OutOfStockException;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.service.ProductSKUService;
import com.sarabrandserver.user.service.ClientService;
import com.sarabrandserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ShoppingSessionRepo shoppingSessionRepo;
    private final CartItemRepo cartItemRepo;
    private final ClientService clientService;
    private final ProductSKUService productSKUService;
    private final CustomUtil customUtil;

    /**
     * Creates a new shopping session or persists details into an existing shopping session
     *
     * @throws CustomNotFoundException if dto property sku does not exist
     * @throws OutOfStockException if dto property qty is greater than inventory
     */
    @Transactional
    public void create(CartDTO dto) {
        var productSKU = this.productSKUService.productSkuBySKU(dto.sku());

        if (dto.qty() > productSKU.getInventory()) {
            throw new OutOfStockException("chosen quantity is out of stock");
        }

        var date = new Date();

        boolean find = this.shoppingSessionRepo
                .shoppingSessionById(dto.session_id())
                .isPresent();

        String principal = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!find) {
            shoppingSessionNotExist(date, productSKU, principal, dto);
        } else {
            shoppingSessionExists(date, productSKU, dto);
        }
    }

    /**
     * Creates a new shopping session
     *
     * @param date      is of type java.util.Date;
     * @param sku       is a ProductSku object
     * @param principal is the user email
     * @param dto       contains necessary details to create a new session
     */
    void shoppingSessionNotExist(Date date, ProductSku sku, String principal, CartDTO dto) {
        // 24 hrs from date
        var expireAt = Duration.ofMillis(Duration.ofHours(24).toMillis());

        this.clientService
                .userByPrincipal(principal)
                .ifPresent(user -> {
                    var shoppingSession = ShoppingSession
                            .builder()
                            .createAt(this.customUtil.toUTC(date))
                            .expireAt(this.customUtil.toUTC(new Date(date.getTime() + expireAt.toMillis())))
                            .sarreBrandUser(user)
                            .cartItems(new HashSet<>())
                            .build();

                    var session = this.shoppingSessionRepo.save(shoppingSession);

                    var cartItem = new CartItem(dto.qty(), session, new HashSet<>());
                    cartItem.addToCart(sku);

                    this.cartItemRepo.save(cartItem);
                });
    }

    void shoppingSessionExists(Date date, ProductSku sku, CartDTO dto) {
        var session = this.shoppingSessionRepo
                .shoppingSessionById(dto.session_id())
                .orElseThrow(() -> new CustomNotFoundException("invalid shopping session"));

        Optional<CartItem> cartItem = this.cartItemRepo
                .cartItemBySKU(dto.sku());

        if (cartItem.isPresent()) {
            // TODO update qty
            return;
        }



    }

}
