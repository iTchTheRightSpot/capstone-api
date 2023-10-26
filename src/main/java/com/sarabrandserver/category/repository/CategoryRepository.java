package com.sarabrandserver.category.repository;

import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.projection.CategoryPojo;
import com.sarabrandserver.product.projection.ProductPojo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<ProductCategory, Long> {
    @Query(value = """
    SELECT
    c.uuid AS uuid,
    c.categoryName AS category,
    c.createAt AS created,
    c.modifiedAt AS modified,
    c.isVisible AS visible
    FROM ProductCategory c
    """)
    List<CategoryPojo> fetchCategoriesWorker();

    /** Equivalent sql statement because jpa can be confusing at times haha
     * select parent.category_name, group_concat(child.category_name)
     * from product_category parent
     * left join product_category child on parent.category_id = child.parent_category_id
     * where parent.parent_category_id is null
     * group by parent.category_name, parent.created_at
     * order by parent.created_at
     * JPA
     * SELECT parent.categoryName AS category, GROUP_CONCAT(child.categoryName) AS sub
     * FROM ProductCategory parent
     * LEFT JOIN ProductCategory child ON parent.categoryId = child.productCategory.categoryId
     * WHERE parent.productCategory.categoryId IS NULL AND parent.isVisible = true
     * GROUP BY parent.categoryName
     * ORDER BY parent.createAt
     * */
    @Query(value = """
    SELECT
    c.categoryName AS category,
    c.uuid AS uuid
    FROM ProductCategory c
    WHERE c.isVisible = true
    """)
    List<CategoryPojo> fetchCategoriesClient();

    @Query("SELECT pc FROM ProductCategory pc WHERE pc.categoryName = :name")
    Optional<ProductCategory> findByName(@Param(value = "name") String name);

    @Query(value = """
    SELECT COUNT(p.productId)
    FROM Product p
    WHERE p.productCategory.uuid = :uuid
    """)
    int productsAttached(String uuid);

    @Query("SELECT c FROM ProductCategory c WHERE c.uuid = :uuid")
    Optional<ProductCategory> findByUuid(@Param(value = "uuid") String uuid);

    // Validate if category name exists but is not associated
    // to @param uuid
    @Query(value = """
    SELECT
    COUNT(pc.categoryId)
    FROM ProductCategory pc
    WHERE pc.categoryName = :name AND pc.uuid != :uuid
    """)
    int duplicateCategoryForUpdate(String uuid, String name);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
        UPDATE ProductCategory pc
        SET pc.modifiedAt = :date, pc.categoryName = :name, pc.isVisible = :visible
        WHERE pc.uuid = :uuid
    """)
    void update(
            @Param(value = "date") Date date,
            @Param(value = "name") String name,
            @Param(value = "visible") boolean visible,
            @Param(value = "uuid") String id
    );

    @Query(value = """
    SELECT
    p.uuid as uuid,
    p.name as name,
    (SELECT
    c.currency
    FROM PriceCurrency c
    WHERE p.productId = c.product.productId AND c.currency = :currency
    ) AS currency,
    (SELECT
    c.price
    FROM PriceCurrency c
    WHERE p.productId = c.product.productId AND c.currency = :currency
    ) AS price,
    p.defaultKey as key
    FROM Product p
    INNER JOIN ProductCategory c ON p.productCategory.categoryId = c.categoryId
    WHERE c.uuid = :uuid
    """)
    Page<ProductPojo> allProductsByCategory(String uuid, Pageable page);

}
