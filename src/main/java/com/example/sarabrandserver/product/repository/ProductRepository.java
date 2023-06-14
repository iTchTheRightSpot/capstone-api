package com.example.sarabrandserver.product.repository;

import com.example.sarabrandserver.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT p FROM Product p WHERE p.name = :name")
    Optional<Product> findByProductName(@Param(value = "name") String name);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Product p SET p.deletedAt = :date WHERE p.name = :name")
    void custom_delete(@Param(value = "date") Date date, @Param(value = "name") String name);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Product p SET p.name = :name WHERE p.productId = :id")
    void updateName(@Param(value = "name") String name, @Param(value = "id") long id);

}
