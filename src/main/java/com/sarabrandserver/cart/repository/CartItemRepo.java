package com.sarabrandserver.cart.repository;

import com.sarabrandserver.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, Long> {

    @Query(value = """
    SELECT c FROM CartItem c
    INNER JOIN ProductSku s ON c.cartId = s.cartItem.cartId
    WHERE s.sku = :sku
    """)
    Optional<CartItem> cartItemBySKU(String sku);

}