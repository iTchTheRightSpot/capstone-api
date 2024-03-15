package dev.capstone.product.repository;

import dev.capstone.enumeration.SarreCurrency;
import dev.capstone.product.entity.PriceCurrency;
import dev.capstone.product.projection.PriceCurrencyPojo;
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
    c.currency AS currency,
    c.price AS price
    FROM Product p
    INNER JOIN PriceCurrency c ON p.productId = c.product.productId
    WHERE p.uuid = :uuid AND c.currency = :currency
    GROUP BY p.name, p.description, c.currency, c.price
    """)
    Optional<PriceCurrencyPojo> priceCurrencyByProductUuidAndCurrency(String uuid, SarreCurrency currency);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    UPDATE PriceCurrency c
    SET
    c.price = :price
    WHERE c.product.uuid = :uuid AND c.currency = :currency
    """)
    void updateProductPriceByProductUuidAndCurrency(String uuid, BigDecimal price, SarreCurrency currency);

}