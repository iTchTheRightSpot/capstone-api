package com.emmanuel.sarabrandserver.product.repository;

import com.emmanuel.sarabrandserver.product.entity.ProductSizeInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSizeInventoryRepository extends JpaRepository<ProductSizeInventory, Long> { }
