package com.example.sarabrandserver.collection.repository;

import com.example.sarabrandserver.collection.projection.CollectionPojo;
import com.example.sarabrandserver.collection.entity.ProductCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<ProductCollection, Long> {

    @Query(value = "SELECT c FROM ProductCollection c WHERE c.collection = :collection")
    Optional<ProductCollection> findByName(@Param(value = "collection") String collection);

    @Query(value = "SELECT c.collection AS collection FROM ProductCollection c")
    List<CollectionPojo> getAll();

}
