package dev.webserver.product;

import dev.webserver.enumeration.CapstoneCurrency;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductPriceCurrencyRepository extends CrudRepository<ProductPriceCurrency, Long> {

    @Query("""
    SELECT
        p.name AS name,
        p.description AS description,
        c.currency AS currency,
        c.price AS price
    FROM product p
    INNER JOIN product_price_currency c ON p.product_id = c.product_id
    WHERE p.uuid = :uuid AND c.currency = :#{#currency.name()}
    GROUP BY p.name, p.description, c.currency, c.price
    """)
    Optional<PriceCurrencyDbMapper> priceCurrencyByProductUuidAndCurrency(final String uuid, final CapstoneCurrency currency);

    @Transactional
    @Modifying
    @Query("""
    UPDATE product_price_currency c
    INNER JOIN product p ON p.product_id = c.product_id
    SET c.price = :price
    WHERE p.uuid = :uuid AND c.currency = :#{#currency.name()}
    """)
    void updateProductPriceByProductUuidAndCurrency(final String uuid, final BigDecimal price, final CapstoneCurrency currency);

}