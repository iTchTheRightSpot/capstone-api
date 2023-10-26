package com.sarabrandserver.product.repository;

import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.product.entity.PriceCurrency;
import com.sarabrandserver.product.projection.PriceCurrencyPojo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface PriceCurrencyRepo extends JpaRepository<PriceCurrency, Long> {

    @Query("""
    SELECT
    p.name AS name,
    p.description AS description,
    (SELECT c.currency FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency ) AS currency,
    (SELECT c.price FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency ) AS price
    FROM Product p
    WHERE p.uuid = :uuid
    """)
    Optional<PriceCurrencyPojo> priceCurrencyByProductUUIDAndCurrency(String uuid, SarreCurrency currency);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    UPDATE PriceCurrency c
    SET
    c.price = :price
    WHERE c.product.uuid = :uuid AND c.currency = :currency
    """)
    void updatePriceByProductUUID(String uuid, BigDecimal price, SarreCurrency currency);

}