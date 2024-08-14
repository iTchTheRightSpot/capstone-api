package dev.webserver.cart;

import dev.webserver.enumeration.CapstoneCurrency;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IShoppingSessionRepository extends CrudRepository<ShoppingSession, Long> {

    @Query("SELECT * FROM shopping_session WHERE cookie = :cookie")
    Optional<ShoppingSession> shoppingSessionByCookie(final String cookie);

    @Transactional
    @Modifying
    @Query(value = "UPDATE shopping_session SET expire_at = :d WHERE cookie = :cookie")
    void updateShoppingSessionExpiry(final String cookie, final LocalDateTime d);

    @Query(value = """
    SELECT
        p.uuid AS uuid,
        p.default_image_key AS image_key,
        p.name AS name,
        p.weight AS weight,
        p.weight_type AS weight_type,
        s.session_id AS session_id,
        cur.currency AS currency,
        cur.price AS price,
        d.colour AS colour,
        ps.size AS size,
        ps.sku AS sku,
        c.qty AS qty
    FROM shopping_session s
    INNER JOIN cart c ON s.session_id = c.session_id
    INNER JOIN product_sku ps ON c.sku_id = ps.sku_id
    INNER JOIN product_detail d ON ps.detail_id = d.detail_id
    INNER JOIN product p ON d.product_id = p.product_id
    INNER JOIN product_price_currency cur ON p.product_id = cur.product_id
    WHERE s.cookie = :cookie AND cur.currency = :currency AND d.is_visible IS TRUE
    GROUP BY p.uuid, s.session_id, p.default_image_key, p.name, cur.currency, cur.price, d.colour, ps.size, ps.sku, c.qty
    """)
    List<CartDbMapper> cartItemsByCookieValue(final CapstoneCurrency currency, final String cookie);

    @Query("SELECT * FROM shopping_session WHERE expire_at <= :d")
    List<ShoppingSession> allExpiredShoppingSession(final LocalDateTime d);

}