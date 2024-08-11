package dev.webserver.product;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.util.Page;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// TODO update all storefront methods here to take into consideration category visibility
public interface ProductRepository extends CrudRepository<Product, Long> {

    @Query(value = "SELECT * FROM product p WHERE p.name = :name")
    Optional<Product> productByName(final String name);

    @Query(value = "SELECT * FROM product p WHERE p.uuid = :uuid")
    Optional<Product> productByUuid(final String uuid);

    @Query(value = "SELECT COUNT(p.product_id) FROM product p WHERE p.name = :name AND p.uuid != :uuid")
    int nameNotAssociatedToUuid(final String uuid, final String name);

    @Transactional
    @Modifying
    @Query("DELETE FROM product p WHERE p.uuid = :uuid")
    void deleteByProductUuid(final String uuid);

    @Query(value = "SELECT COUNT(product_id) FROM product")
    int countAllProductsForAdminFront();

    @Query(value = """
    SELECT
        p.uuid AS uuid,
        p.name AS name,
        p.description AS description,
        p.default_image_key AS image_key,
        p.weight AS weight,
        p.weight_type AS weight_type,
        c.currency AS currency,
        c.price AS price,
        cat.name AS category_name
    FROM product p
    INNER JOIN category cat ON p.category_id = cat.category_id
    INNER JOIN product_price_currency c ON p.product_id = c.product_id
    WHERE c.currency = :#{#currency.name()}
    GROUP BY p.uuid, p.name, p.description, p.default_image_Key, p.weight, p.weight_type, c.currency, c.price, cat.name
    LIMIT :#{#page.size()} OFFSET :#{#page.offset()}
    """)
    List<ProductDbMapper> allProductsForAdminFront(final Page page, final SarreCurrency currency);

    @Query(value = """
    WITH RECURSIVE rec_category (id) AS
    (
        SELECT category_id FROM category
        WHERE parent_id IS NULL AND is_visible IS TRUE
        UNION ALL
        SELECT c.category_id FROM rec_category rec
        INNER JOIN category c ON rec.id = c.parent_id
        WHERE c.is_visible IS TRUE
    )
    SELECT
        COUNT(DISTINCT p.product_id)
    FROM rec_category r
    INNER JOIN product p ON p.category_id = r.id
    INNER JOIN product_detail pd ON pd.product_id = p.product_id
    INNER JOIN product_price_currency c ON p.product_id = c.product_id
    INNER JOIN product_sku sku ON pd.detail_id = sku.detail_id
    WHERE pd.is_visible = TRUE AND sku.inventory > 0
    """)
    int countAllProductsStoreFront();

    @Query(value = """
    WITH RECURSIVE rec_category (id, name) AS
    (
        SELECT category_id, name FROM category
        WHERE parent_id IS NULL AND is_visible IS TRUE
        UNION ALL
        SELECT c.category_id, c.name FROM rec_category rec
        INNER JOIN category c ON rec.id = c.parent_id
        WHERE c.is_visible IS TRUE
    )
    SELECT
        p.uuid AS uuid,
        p.name AS name,
        p.description AS description,
        p.default_image_key AS image_key,
        p.weight AS weight,
        p.weight_type AS weight_type,
        c.currency AS currency,
        c.price AS price,
        r.name AS category_name
    FROM rec_category r
    INNER JOIN product p ON r.id = p.category_id
    INNER JOIN product_detail pd ON pd.product_id = p.product_id
    INNER JOIN product_price_currency c ON p.product_id = c.product_id
    INNER JOIN product_sku sku ON pd.detail_id = sku.detail_id
    WHERE pd.is_visible = TRUE AND sku.inventory > 0 AND c.currency = :#{#currency.name()}
    GROUP BY p.uuid, p.name, p.description, p.default_image_key, p.weight, p.weight_type, c.currency, c.price, r.name
    LIMIT :#{#page.size()} OFFSET :#{#page.offset()}
    """)
    List<ProductDbMapper> allProductsByCurrencyClient(final Page page, final SarreCurrency currency);

    @Transactional
    @Modifying
    @Query(value = """
    UPDATE product p
    SET
    p.name = :name,
    p.description = :desc,
    p.category_id = :categoryId,
    p.weight = :weight
    WHERE p.uuid = :uuid
    """)
    void updateProduct(
            final String uuid,
            final String name,
            final String desc,
            final BigDecimal weight,
            final Long categoryId
    );

    @Query(value = """
    SELECT
        img.image_key as image_key
    FROM product_image img
    INNER JOIN product_detail pd ON img.detail_id = pd.detail_id
    INNER JOIN product p ON p.product_id = pd.product_id
    WHERE p.uuid = :uuid
    """)
    List<ProductImageDbMapper> productImagesByProductUuid(final String uuid);

    // https://www.w3schools.com/sql/sql_like.asp
    @Query("""
    SELECT
        p.uuid AS uuid,
        p.name AS name,
        p.default_image_key AS image_key,
        p.weight AS weight,
        p.weight_type AS weight_type,
        c.price AS price,
        c.currency AS currency,
        cat.name AS category_name
    FROM product p
    INNER JOIN category cat ON p.category_id = cat.category_id
    INNER JOIN product_price_currency c ON p.product_id = c.product_id
    INNER JOIN product_detail pd ON p.product_id = pd.product_id
    INNER JOIN product_sku sku ON pd.detail_id = sku.detail_id
    WHERE p.name LIKE :name AND sku.inventory > 0 AND c.currency = :#{#currency.name()}
    GROUP BY p.uuid, p.name, p.default_image_key, p.weight, p.weight_type, c.currency, c.price, cat.name
    """)
    List<ProductDbMapper> productsByNameAndCurrency(final String name, final SarreCurrency currency);

}