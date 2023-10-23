package com.sarabrandserver.cart.repository;

import com.sarabrandserver.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, Long> {

    @Query(value = """
    SELECT c FROM CartItem c
    INNER JOIN ShoppingSession s ON c.shoppingSession.shoppingSessionId = s.shoppingSessionId
    WHERE s.sarreBrandUser.email = :principal AND c.sku = :sku
    """)
    Optional<CartItem> cartItemBySKUAndPrincipal(String sku, String principal);

    @Query(value = """
    UPDATE CartItem c
    SET c.qty = :qty
    WHERE c.cartId = :id
    """)
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void updateCartQtyByCartId(long id, int qty);

}