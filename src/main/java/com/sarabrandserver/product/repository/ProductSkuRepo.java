package com.sarabrandserver.product.repository;

import com.sarabrandserver.product.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductSkuRepo extends JpaRepository<ProductSku, Long> {

    @Query(value = "SELECT p FROM ProductSku p WHERE p.sku = :sku")
    Optional<ProductSku> findBySku(String sku);

    @Query("""
    SELECT COUNT (s.skuId)
    FROM ProductSku s
    INNER JOIN OrderDetail o ON s.sku = o.sku
    WHERE s.sku = :sku
    """)
    int itemBeenBought(String sku);

    @Query("""
    SELECT COUNT (s.skuId)
    FROM ProductSku s
    INNER JOIN CartItem c ON s.sku = c.sku
    WHERE s.sku = :sku
    """)
    int itemContainsCart(String sku);

}
