package com.example.sarabrandserver.category.repository;

import com.example.sarabrandserver.category.entity.ProductCategory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<ProductCategory, Long> {

    @Query("""
        SELECT COUNT(p.categoryId)
        FROM ProductCategory p
        LEFT JOIN ProductCategory p2
        ON p.categoryId = p2.categoryId
        WHERE p.categoryName = :name
        OR p2.categoryName IN :list
    """)
    int duplicateCategoryName(@Param(value = "name") String name, @Param(value = "list") List<String> list);

    /**
     * Because category is a self join, the query below selects categories that do not have any parents and have not
     * been deleted.
     * @return List of Object of param Long representing categoryId and String representing categoryName
     * */
    @Query(value = """
    SELECT p.categoryId, p.categoryName
    FROM ProductCategory p
    LEFT JOIN ProductCategory p2
    ON p.categoryId = p2.categoryId
    WHERE p.productCategory.categoryId IS NULL
    AND p.deletedAt IS NULL
    ORDER BY p.createAt
    """)
    List<Object> getParentCategoriesWithIdNull();

    /**
     * Query fetches all child nodes of a parent
     * @return List of String categoryName
     * */
    @Query(value = """
    SELECT p.categoryName
    FROM ProductCategory p
    WHERE p.productCategory.categoryId = :id
    AND p.deletedAt IS NULL
    ORDER BY p.createAt
    """)
    List<String> getChildCategoriesWhereDeletedIsNull(@Param(value = "id") Long id);

    @Query("SELECT pc FROM ProductCategory pc WHERE pc.categoryName = :name")
    Optional<ProductCategory> findByName(@Param(value = "name") String name);

    @Modifying
    @Transactional
    @Query("""
        UPDATE ProductCategory pc
        SET pc.modifiedAt = :date, pc.categoryName = :newName
        WHERE pc.categoryName = :oldName
    """)
    void update(
            @Param(value = "date") Date date,
            @Param(value = "newName") String newName,
            @Param(value = "oldName") String oldName
    );

    @Modifying
    @Transactional
    @Query("UPDATE ProductCategory pc SET pc.modifiedAt = :date, pc.deletedAt = :date WHERE pc.categoryName = :name")
    void custom_delete(@Param(value = "date") Date date, @Param(value = "name") String name);

}
