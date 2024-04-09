package dev.webserver.category.repository;

import dev.webserver.category.entity.ProductCategory;
import dev.webserver.category.projection.CategoryPojo;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.entity.Product;
import dev.webserver.product.projection.ProductPojo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * contains native query
 * */
@Repository
public interface CategoryRepository extends JpaRepository<ProductCategory, Long> {

    @Query("SELECT c FROM ProductCategory c WHERE c.name = :name")
    Optional<ProductCategory> findByName(@Param(value = "name") String name);

    /**
     * Validates if 1 or more rows depends on {@link ProductCategory} by
     * {@param categoryId} as its parentId.
     *
     * @param id is {@link ProductCategory} property categoryId.
     * @return {@code 0 or greater than 0} where 0 means it doesn't have a
     * child {@link ProductCategory} and greater than 0 means 1 or more rows
     * depends on it as a parentId.
     * */
    @Query(value = """
    SELECT COUNT (c.categoryId)
    FROM ProductCategory c
    WHERE c.parentCategory.categoryId = :id
    """)
    int validateCategoryIsAParent(long id);

    @Query(value = """
    SELECT
    COUNT(pc.categoryId)
    FROM ProductCategory pc
    WHERE pc.name = :name AND pc.categoryId != :id
    """)
    int onDuplicateCategoryName(long id, String name);

    /**
     * Updates name and isVisible properties of a {@link ProductCategory}.
     * */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("""
    UPDATE ProductCategory pc
    SET  pc.name = :name, pc.isVisible = :visible
    WHERE pc.categoryId = :id
    """)
    void update(
            @Param(value = "name") String name,
            @Param(value = "visible") boolean visible,
            @Param(value = "id") long id
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM ProductCategory c WHERE c.categoryId = :id")
    void deleteProductCategoryById(long id);

    /**
     * Updates a {@link ProductCategory} parentId to a new categoryId.
     * */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("""
    UPDATE ProductCategory p
    SET p.parentCategory.categoryId = :parentId
    WHERE p.categoryId = :categoryId
    """)
    void updateCategoryParentIdBasedOnCategoryId(long categoryId, long parentId);

    /**
     * Using Common Table Expression (CTE) in native sql query, we recursively update the is_visible
     * property of {@link ProductCategory} to false.
     *
     * @param categoryId is all {@link ProductCategory} who have their parent_category_id equalling.
     * */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(nativeQuery = true, value = """
    WITH RECURSIVE category (id) AS
    (
        SELECT c.category_id FROM product_category AS c WHERE c.parent_category_id = :categoryId
        UNION ALL
        SELECT pc.category_id FROM category cat INNER JOIN product_category pc ON cat.id = pc.parent_category_id
    )
    UPDATE product_category c, (SELECT rec.id FROM category rec) AS rec
    SET c.is_visible = 0
    WHERE c.category_id = rec.id
    """)
    void updateAllChildrenVisibilityToFalse(long categoryId);

    @Query(value = """
    SELECT
    p.uuid as uuid,
    p.name as name,
    p.defaultKey as image,
    curr.currency AS currency,
    curr.price AS price
    FROM Product p
    INNER JOIN ProductCategory c ON p.productCategory.categoryId = c.categoryId
    INNER JOIN PriceCurrency curr ON p.productId = curr.product.productId
    WHERE c.categoryId = :categoryId AND curr.currency = :currency
    GROUP BY p.uuid, p.name, p.description, p.defaultKey, curr.currency, curr.price
    """)
    Page<ProductPojo> allProductsByCategoryIdAdminFront(
            long categoryId,
            SarreCurrency currency,
            Pageable page
    );

    /**
     * Using native sql query, we retrieve a paginated list of {@link Product}s based on the specified
     * {@link ProductCategory} and its children, filtered by currency, inventory availability, and visibility.
     * <p/>
     * We use Common Table Expression (CTE) to recursively fetch the specified {@link ProductCategory} and
     * all its subcategories. With the obtained categories, we retrieve all products associated with them.
     *
     * @param categoryId The primary key of {@link ProductCategory}.
     * @param currency The currency in which prices are displayed, represented by a {@link SarreCurrency} enum
     *                 value.
     * @param page The pagination information, represented by a {@link Pageable} object.
     * @return Leveraging Spring Data Projection, a paginated {@link Page} containing {@link ProductPojo} objects.
     * */
    @Query(nativeQuery = true, value = """
    WITH RECURSIVE category (id) AS
    (
        SELECT c.category_id FROM product_category AS c WHERE c.category_id = :categoryId
        UNION ALL
        SELECT pc.category_id FROM category cat INNER JOIN product_category pc ON cat.id = pc.parent_category_id
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
    INNER JOIN price_currency pr ON p.product_id = pr.product_id
    INNER JOIN product_detail d ON p.product_id = d.product_id
    INNER JOIN product_sku s ON d.detail_id = s.detail_id
    WHERE pr.currency = :#{#currency.name()} AND s.inventory > 0 AND d.is_visible = TRUE
    GROUP BY p.uuid, p.name, p.description, p.default_image_key, pr.currency, pr.price
    """)
    Page<ProductPojo> allProductsByCategoryIdWhereInStockAndIsVisible(
            long categoryId,
            SarreCurrency currency,
            Pageable page
    );

    /**
     * Retrieves all {@link ProductCategory} objects. Then maps the
     * objects to a {@link CategoryPojo} using Spring Data Projection.
     * */
    @Query(value = """
    SELECT
    c.categoryId AS id,
    c.name AS name,
    c.isVisible AS status,
    c.parentCategory.categoryId AS parent
    FROM ProductCategory c
    """)
    List<CategoryPojo> allCategories();

}
