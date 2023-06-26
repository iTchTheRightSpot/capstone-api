package com.emmanuel.sarabrandserver.product.repository;

import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDetailRepo extends JpaRepository<ProductDetail, Long> { }
