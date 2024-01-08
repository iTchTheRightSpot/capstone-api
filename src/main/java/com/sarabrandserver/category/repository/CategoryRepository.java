package com.sarabrandserver.category.repository;

import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.projection.CategoryPojo;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.product.projection.ProductPojo;
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
     * Using native sql query, method allows deleting a {@code ProductCategory}
     * only after validating if {@param id} has no nested
     * {@code ProductCategory}/children attached and no {@code Product} is
     * attached to {@param id} or its children.
     *
     * @param id is {@code ProductCategory} to be deleted
     * @return if return value is 0, then none of the conditions apply else if
     * greater than zero, all the conditions in the description applies.
     * */
    @Query(nativeQuery = true, value = """
    WITH RECURSIVE category (id, parent) AS
    (
        SELECT
            c.category_id,
            c.parent_category_id
        FROM product_category c
        WHERE c.category_id = :id
        UNION ALL
        SELECT
        pc.category_id,
        pc.parent_category_id
        FROM category cat
        INNER JOIN product_category pc
        ON cat.id = pc.parent_category_id
    )
    SELECT COUNT(p.product_id)
    FROM category c1
    INNER JOIN product p
    ON c1.id = p.category_id
    """)
    int validateProductAttached(long id);

    @Query(nativeQuery = true, value = """
    WITH RECURSIVE category (id, parent) AS
    (
        SELECT
            c.category_id,
            c.parent_category_id
        FROM product_category c
        WHERE c.category_id = :id
        UNION ALL
        SELECT
        pc.category_id,
        pc.parent_category_id
        FROM category cat
        INNER JOIN product_category pc
        ON cat.id = pc.parent_category_id
    )
    SELECT COUNT(c1.id) FROM category c1
    """)
    int validateContainsSubCategory(long id);

    @Query(value = """
    SELECT
    COUNT(pc.categoryId)
    FROM ProductCategory pc
    WHERE pc.name = :name AND pc.categoryId != :id
    """)
    int onDuplicateCategoryName(long id, String name);

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
    @Query("UPDATE ProductCategory c SET c.isVisible = :visible WHERE c.categoryId = :id")
    void upVisibility(long id, boolean visible);

    @Query(value = """
    SELECT
    p.uuid as uuid,
    p.name as name,
    (SELECT c.currency FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency) AS currency,
    (SELECT c.price FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency) AS price,
    p.defaultKey as key
    FROM Product p
    INNER JOIN ProductCategory c ON p.productCategory.categoryId = c.categoryId
    WHERE c.categoryId = :id
    """)
    Page<ProductPojo> allProductsByCategory(SarreCurrency currency, long id, Pageable page);

    /**
     * Selects all {@code ProductCategory} objects that have a parent category id null.
     *
     * @return a list of {@code ProductCategory}
     * */
    @Query("SELECT c FROM ProductCategory c WHERE c.parentCategory.categoryId IS NULL")
    List<ProductCategory> superCategories();

    @Query(value = """
    SELECT
    p.uuid AS uuid,
    p.name AS name,
    p.description AS description,
    (SELECT c.currency FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency) AS currency,
    (SELECT c.price FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency) AS price,
    p.defaultKey AS key,
    pc.name AS category
    FROM Product p
    INNER JOIN ProductCategory pc ON p.productCategory.categoryId = pc.categoryId
    INNER JOIN ProductDetail pd ON p.productId = pd.product.productId
    INNER JOIN ProductSku sku ON pd.productDetailId = sku.productDetail.productDetailId
    WHERE pd.isVisible = TRUE AND sku.inventory > 0 AND pc.categoryId = :id
    GROUP BY p.uuid, p.name, p.description, p.defaultKey
    """)
    Page<ProductPojo> productsByCategoryClient(long id, SarreCurrency currency, Pageable page);

    /**
     * Using native sql query and Spring Data projection, method returns all
     * children of specified {@code ProductCategory} {@code id}.
     * For more about using common table expression CTE visit
     * <a href="https://dev.mysql.com/doc/refman/8.0/en/with.html#common-table-expressions-recursive">...</a>
     *
     * @param id would be a {@code ProductCategory}
     * @return a list of {@code CategoryPojo} object
     * */
    @Query(nativeQuery = true, value = """
    WITH RECURSIVE category (id, name, status, parent) AS
    (
        SELECT
            c.category_id,
            c.name,
            c.is_visible,
            c.parent_category_id
        FROM product_category c
        WHERE c.parent_category_id = :id
        UNION ALL
        SELECT
            pc.category_id,
            pc.name,
            pc.is_visible,
            pc.parent_category_id
        FROM category cat
        INNER JOIN product_category pc
        ON cat.id = pc.parent_category_id
    )
    SELECT
        c.id AS id,
        c.name AS name,
        c.status AS visible,
        c.parent AS parent
    FROM category c
    WHERE c.status IS TRUE;
    """)
    List<CategoryPojo> all_categories_store_front(long id);

    /**
     * Using native sql query and Spring Data projection, method returns all
     * children of specified {@code ProductCategory} {@code id}.
     * For more about using common table expression CTE visit
     * <a href="https://dev.mysql.com/doc/refman/8.0/en/with.html#common-table-expressions-recursive">...</a>
     *
     * @param id would be a {@code ProductCategory}
     * @return a list of {@code CategoryPojo} object
     * */
    @Query(nativeQuery = true, value = """
    WITH RECURSIVE category (id, name, status, parent) AS
    (
        SELECT
            c.category_id,
            c.name,
            c.is_visible,
            c.parent_category_id
        FROM product_category c
        WHERE c.parent_category_id = :id
        UNION ALL
        SELECT
            pc.category_id,
            pc.name,
            pc.is_visible,
            pc.parent_category_id
        FROM category cat
        INNER JOIN product_category pc
        ON cat.id = pc.parent_category_id
    )
    SELECT
        c.id AS id,
        c.name AS name,
        c.status AS visible,
        c.parent AS parent
    FROM category c;
    """)
    List<CategoryPojo> all_categories_admin_front(long id);

}
