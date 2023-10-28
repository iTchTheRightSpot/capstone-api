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
    SELECT c
    FROM CartItem c
    INNER JOIN ShoppingSession s ON c.shoppingSession.shoppingSessionId = s.shoppingSessionId
    WHERE s.shoppingSessionId = :id AND c.sku = :sku
    """)
    Optional<CartItem> cart_item_by_shopping_session_id_and_sku(long id, String sku);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
    UPDATE CartItem c
    SET c.qty = :qty
    WHERE c.cartId = :id
    """)
    void updateCartQtyByCartId(long id, int qty);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
    DELETE FROM CartItem c
    WHERE c.shoppingSession.sarreBrandUser.email = :principal AND c.sku = :sku
    """)
    void deleteByCartSku(String principal, String sku);

}