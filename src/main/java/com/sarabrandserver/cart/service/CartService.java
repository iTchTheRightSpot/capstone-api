package com.sarabrandserver.cart.service;

import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.CartRepository;
import com.sarabrandserver.exception.CustomInvalidFormatException;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.OutOfStockException;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.service.ProductSKUService;
import com.sarabrandserver.user.service.ClientService;
import com.sarabrandserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final ClientService clientService;
    private final ProductSKUService productSKUService;
    private final CustomUtil customUtil;

    /**
     * Creates or persists new items into cart
     *
     * @throws CustomNotFoundException if sku does not exist
     */
    @Transactional
    public void create(CartDTO dto) {
        var productSKU = this.productSKUService.productSkuBySKU(dto.sku());

        if (dto.qty() > productSKU.getInventory()) {
            throw new OutOfStockException("chosen quantity is out of stock");
        }

        var date = new Date();

        boolean find = this.cartRepository
                .shoppingSessionById(dto.session_id())
                .isPresent();

        String principal = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!find) {
            shoppingSessionNotExist(date, productSKU, principal, dto);
        } else {
            shoppingSessionExists(date, productSKU, principal, dto);
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
    private void shoppingSessionNotExist(Date date, ProductSku sku, String principal, CartDTO dto) {
        // 24 hrs from date
        var expireAt = Duration.ofMillis(Duration.ofHours(24).toMillis());

        this.clientService
                .userByPrincipal(principal)
                .ifPresent(user -> {
                    var session = ShoppingSession
                            .builder()
                            .qty(dto.qty())
                            .createAt(this.customUtil.toUTC(date))
                            .expireAt(this.customUtil.toUTC(new Date(date.getTime() + expireAt.toMillis())))
                            .sarreBrandUser(user)
                            .skus(new HashSet<>())
                            .build();

                    // TODO convert relationship to many to many
                    session.persist(sku);

                    this.cartRepository.save(session);
                });
    }

    private void shoppingSessionExists(Date date, ProductSku sku, String principal, CartDTO dto) {
        if (dto.session_id() == null) {
            throw new CustomInvalidFormatException("session id cannot be null");
        }

        // TODO persist ProductSKU to users session
    }

}
