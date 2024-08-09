package dev.webserver.product;

import dev.webserver.enumeration.SarreCurrency;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

public interface PriceCurrencyRepository extends CrudRepository<PriceCurrency, Long> {

    @Query("""
    SELECT
        p.name AS name,
        p.description AS description,
        c.currency AS currency,
        c.price AS price
    FROM product p
    INNER JOIN price_currency c ON p.product_id = c.product_id
    WHERE p.uuid = :uuid AND c.currency = :#{#currency.name()}
    GROUP BY p.name, p.description, c.currency, c.price
    """)
    Optional<PriceCurrencyDbMapper> priceCurrencyByProductUuidAndCurrency(String uuid, SarreCurrency currency);

    @Transactional
    @Modifying
    @Query("UPDATE price_currency SET price = :price WHERE uuid = :uuid AND currency = :#{#currency.name()}")
    void updateProductPriceByProductUuidAndCurrency(String uuid, BigDecimal price, SarreCurrency currency);

}