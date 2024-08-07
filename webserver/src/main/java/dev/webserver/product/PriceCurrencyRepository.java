package dev.webserver.product;

import dev.webserver.enumeration.SarreCurrency;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

interface PriceCurrencyRepository extends CrudRepository<PriceCurrency, Long> {

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
    Optional<PriceCurrencyProjection> priceCurrencyByProductUuidAndCurrency(String uuid, SarreCurrency currency);

    @Transactional
    @Modifying
    @Query("""
    UPDATE PriceCurrency c
    SET
    c.price = :price
    WHERE c.product.uuid = :uuid AND c.currency = :currency
    """)
    void updateProductPriceByProductUuidAndCurrency(String uuid, BigDecimal price, SarreCurrency currency);

}