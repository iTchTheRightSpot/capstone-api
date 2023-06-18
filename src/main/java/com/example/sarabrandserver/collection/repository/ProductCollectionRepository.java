package com.example.sarabrandserver.collection.repository;

import com.example.sarabrandserver.collection.entity.ProductCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCollectionRepository extends JpaRepository<ProductCollection, Long> {}
