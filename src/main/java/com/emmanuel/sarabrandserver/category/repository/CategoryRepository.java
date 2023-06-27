package com.emmanuel.sarabrandserver.category.repository;

import com.emmanuel.sarabrandserver.category.entity.ProductCategory;
import com.emmanuel.sarabrandserver.category.projection.CategoryPojo;
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
    SELECT c.categoryName AS category, c.createAt AS created, c.modifiedAt AS modified, c.isVisible AS visible
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
     * */
    @Query(value = """
    SELECT parent.categoryName AS category, GROUP_CONCAT(child.categoryName) AS sub
    FROM ProductCategory parent
    LEFT JOIN ProductCategory child ON parent.categoryId = child.productCategory.categoryId
    WHERE parent.productCategory.categoryId IS NULL AND parent.isVisible = true
    GROUP BY parent.categoryName
    ORDER BY parent.createAt
    """)
    List<CategoryPojo> fetchCategoriesClient();

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
