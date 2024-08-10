package dev.webserver.category;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.Product;
import dev.webserver.product.ProductDbMapper;
import dev.webserver.util.Page;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends CrudRepository<Category, Long> {

    @Query("SELECT * FROM category c WHERE c.name = :name")
    Optional<Category> findByName(final String name);

    @Query("SELECT COUNT (c.category_id) FROM category c WHERE c.parent_id = :parentId")
    int validateCategoryIsAParent(final long parentId);

    @Query("SELECT COUNT(pc.category_id) FROM category pc WHERE pc.name = :name AND pc.category_id != :categoryId")
    int onDuplicateCategoryName(final long categoryId, final String name);

    /**
     * Updates name and isVisible properties of a {@link Category}.
     * */
    @Modifying
    @Transactional
    @Query("UPDATE category pc SET  pc.name = :name, pc.is_visible = :visible WHERE pc.category_id = :id")
    void update(
            final String name,
            final boolean visible,
            final long categoryId
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM category c WHERE c.category_id = :categoryId")
    void deleteProductCategoryById(final long categoryId);

    /**
     * Updates a {@link Category} parentId to an existing categoryId.
     * */
    @Modifying
    @Transactional
    @Query("UPDATE category p SET p.parent_id = :parentId WHERE p.category_id = :categoryId")
    void updateCategoryParentId(final long categoryId, final long parentId);

    /**
     * Using Common Table Expression (CTE) in native sql query, we recursively update the is_visible
     * property of {@link Category} to false.
     *
     * @param categoryId is all {@link Category} who have their parent_category_id equalling.
     * */
    @Modifying
    @Transactional
    @Query(value = """
    WITH RECURSIVE rec_category (id) AS
    (
        SELECT c.category_id FROM category AS c WHERE c.parent_id = :categoryId
        UNION ALL
        SELECT pc.category_id FROM rec_category rec INNER JOIN category pc ON rec.id = pc.parent_id
    )
    UPDATE category c, (SELECT rec.id FROM category rec) AS rec
    SET c.is_visible = FALSE
    WHERE c.category_id = rec.id
    """)
    void updateAllChildrenVisibilityToFalse(final long categoryId);

    @Query(value = """
    SELECT
        p.uuid as uuid,
        p.name as name,
        p.default_image_key as image,
        curr.currency AS currency,
        curr.price AS price
    FROM product p
    INNER JOIN category c ON c.categoryId = p.category_id
    INNER JOIN product_price_currency curr ON curr.product_id = p.product_id
    WHERE c.category_id = :categoryId AND curr.currency = :currency
    GROUP BY p.uuid, p.name, p.description, p.default_image_key, curr.currency, curr.price
    LIMIT :#{#page.size()} OFFSET :#{#page.offset()}
    """)
    List<ProductDbMapper> allProductsByCategoryIdAdminFront(
            final long categoryId,
            final SarreCurrency currency,
            final Page page
    );

    // TODO repository test
    @Query(value = """
    SELECT
        COUNT(p.uuid)
    FROM product p
    INNER JOIN category c ON c.categoryId = p.category_id
    INNER JOIN product_price_currency curr ON curr.product_id = p.product_id
    WHERE c.category_id = :categoryId AND curr.currency = :currency
    GROUP BY p.uuid
    """)
    Integer countAllProductsByCategoryIdAdminFront(final long categoryId, final SarreCurrency currency);

    // TODO repository test
    @Query(value = """
    WITH RECURSIVE rec_category (id) AS
    (
        SELECT c.category_id FROM category AS c WHERE c.category_id = :categoryId
        UNION ALL
        SELECT pc.category_id FROM rec_category rec INNER JOIN category pc ON rec.id = pc.parent_id
    )
    SELECT
        COUNT(p.uuid)
    FROM category c1
    INNER JOIN product p ON c1.id = p.category_id
    INNER JOIN product_price_currency pr ON p.product_id = pr.product_id
    INNER JOIN product_detail d ON p.product_id = d.product_id
    INNER JOIN product_sku s ON d.detail_id = s.detail_id
    WHERE pr.currency = :#{#currency.name()} AND s.inventory > 0 AND d.is_visible = TRUE
    GROUP BY p.uuid
    """)
    Integer countAllProductsByCategoryIdWhereInStockAndIsVisible(final long categoryId, final SarreCurrency currency);

    /**
     * Using native sql query, we retrieve a paginated list of {@link Product}s based on the specified
     * {@link Category} and its children, filtered by currency, inventory availability, and visibility.
     * <p/>
     * We use Common Table Expression (CTE) to recursively fetch the specified {@link Category} and
     * all its subcategories. With the obtained categories, we retrieve all products associated with them.
     *
     * @param categoryId The primary key of {@link Category}.
     * @param currency The currency in which prices are displayed, represented by a {@link SarreCurrency} enum
     *                 value.
     * @param page The pagination information, represented by a {@link Page} object.
     * @return Leveraging Spring Data Projection, a paginated {@link Page} containing {@link ProductDbMapper} objects.
     * */
    @Query(value = """
    WITH RECURSIVE rec_category (id) AS
    (
        SELECT c.category_id FROM category AS c WHERE c.category_id = :categoryId
        UNION ALL
        SELECT pc.category_id FROM rec_category rec INNER JOIN category pc ON rec.id = pc.parent_id
    )
    SELECT
        p.uuid AS uuid,
        p.name AS name,
        p.description AS description,
        p.default_image_key AS image,
        pr.currency AS currency,
        pr.price AS price
    FROM category c1
    INNER JOIN product p ON c1.id = p.category_id
    INNER JOIN product_price_currency pr ON p.product_id = pr.product_id
    INNER JOIN product_detail d ON p.product_id = d.product_id
    INNER JOIN product_sku s ON d.detail_id = s.detail_id
    WHERE pr.currency = :#{#currency.name()} AND s.inventory > 0 AND d.is_visible = TRUE
    GROUP BY p.uuid, p.name, p.description, p.default_image_key, pr.currency, pr.price
    LIMIT :#{#page.size()} OFFSET :#{#page.offset()}
    """)
    List<ProductDbMapper> allProductsByCategoryIdWhereInStockAndIsVisible(
            final long categoryId,
            final SarreCurrency currency,
            final Page page
    );

    @Query(value = "SELECT * FROM category")
    List<Category> allCategories();

    // TODO repository test
    @Query("""
    WITH RECURSIVE rec_category (id) AS
    (
        SELECT category_id FROM category WHERE is_visible = TRUE 
        UNION ALL
        SELECT
            cat.category_id
        FROM rec_category rec
        INNER JOIN category cat ON rec.id = cat.parent_id
    )
    SELECT
        c.*
    FROM rec_category rec
    INNER JOIN category c ON rec.id = c.category_id
    """)
    List<Category> allCategoriesStoreFront();

}
