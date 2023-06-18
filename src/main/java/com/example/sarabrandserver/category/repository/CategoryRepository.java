package com.example.sarabrandserver.category.repository;

import com.example.sarabrandserver.category.entity.ProductCategory;
import com.example.sarabrandserver.category.projection.CategoryPojo;
import com.example.sarabrandserver.product.projection.ClientProductPojo;
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
import java.util.Set;

@Repository
public interface CategoryRepository extends JpaRepository<ProductCategory, Long> {
    @Query("""
    SELECT COUNT(p.categoryId)
    FROM ProductCategory p
    WHERE p.categoryName = :name
    OR p.categoryName IN :list
    """)
    int duplicateCategoryName(@Param(value = "name") String name, @Param(value = "list") Set<String> list);

    /** Equivalent sql statement because jpa can be confusing at times hahaha
     * select parent.category_name, group_concat(child.category_name)
     * from product_category parent
     * left join product_category child on parent.category_id = child.parent_category_id
     * where parent.parent_category_id is null
     * group by parent.category_name, parent.created_at
     * order by parent.created_at
     * */
    @Query(value = """
    SELECT parent.categoryName AS category, parent.isVisible AS status, GROUP_CONCAT(child.categoryName) AS sub
    FROM ProductCategory parent
    LEFT JOIN ProductCategory child ON parent.categoryId = child.productCategory.categoryId
    WHERE parent.productCategory.categoryId IS NULL
    GROUP BY parent.categoryName, parent.createAt, parent.isVisible
    ORDER BY parent.createAt
    """)
    List<CategoryPojo> fetchCategories();

    @Query(value = """
    SELECT parent.categoryName AS category, parent.isVisible AS status, GROUP_CONCAT(child.categoryName) AS sub
    FROM ProductCategory parent
    LEFT JOIN ProductCategory child ON parent.categoryId = child.productCategory.categoryId
    WHERE parent.productCategory.categoryId IS NULL AND parent.isVisible = true
    GROUP BY parent.categoryName, parent.createAt, parent.isVisible
    ORDER BY parent.createAt
    """)
    List<CategoryPojo> fetchCategoriesClient();

    @Query(value = """
    SELECT p.name AS name,
    p.description AS desc,
    p.price AS price,
    p.currency AS currency,
    pd.sku AS sku,
    ps.size AS size,
    inv.quantity AS quantity,
    img.imageKey AS image,
    pc.colour AS colour
    FROM ProductCategory cat
    INNER JOIN Product p ON p.productCategory.categoryId = cat.categoryId
    INNER JOIN ProductDetail pd ON p.productId = pd.product.productId
    INNER JOIN ProductSize ps ON pd.productSize.productSizeId = ps.productSizeId
    INNER JOIN ProductInventory inv ON pd.productInventory.productInventoryId = inv.productInventoryId
    INNER JOIN ProductImage img ON pd.productImage.productImageId = img.productImageId
    INNER JOIN ProductColour pc ON pd.productColour.productColourId = pc.productColourId
    WHERE cat.categoryName = :name AND pd.isVisible = true
    """)
    List<ClientProductPojo> fetchByProductName(@Param(value = "name") String name, Pageable pageable);

    @Query("SELECT pc FROM ProductCategory pc WHERE pc.categoryName = :name")
    Optional<ProductCategory> findByName(@Param(value = "name") String name);

    @Query(value = """
    SELECT COUNT(pc.categoryName) FROM ProductCategory pc
    WHERE pc.categoryName = :newName
    """)
    int duplicateCategoryForUpdate(@Param(value = "newName") String newName);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
        UPDATE ProductCategory pc
        SET pc.modifiedAt = :date, pc.categoryName = :newName
        WHERE pc.categoryName = :oldName
    """)
    void update(
            @Param(value = "date") Date date,
            @Param(value = "oldName") String oldName,
            @Param(value = "newName") String newName
    );

}
