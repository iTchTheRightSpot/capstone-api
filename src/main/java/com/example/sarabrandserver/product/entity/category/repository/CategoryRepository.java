package com.example.sarabrandserver.product.entity.category.repository;

import com.example.sarabrandserver.product.entity.category.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<ProductCategory, Long> {

    @Query("""
        SELECT COUNT (pc.categoryId) FROM ProductCategory pc
        WHERE pc.categoryName = :name AND pc.productCategories IN :list
    """)
    int duplicateCategoryName(@Param(value = "name") String name, @Param(value = "list") List<String> list);

    @Query("SELECT pc.categoryName, pc.productCategories FROM ProductCategory pc")
    List<Object> allCategoryName();

    @Query("SELECT pc FROM ProductCategory pc WHERE pc.categoryName = :name")
    Optional<ProductCategory> findByName(@Param(value = "name") String name);

    void update();

}
