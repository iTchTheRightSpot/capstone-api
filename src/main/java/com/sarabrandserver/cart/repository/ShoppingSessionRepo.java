package com.sarabrandserver.cart.repository;

import com.sarabrandserver.cart.entity.ShoppingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingSessionRepo extends JpaRepository<ShoppingSession, Long> {

    @Query("SELECT s FROM ShoppingSession s WHERE s.shoppingSessionId = :id")
    Optional<ShoppingSession> shoppingSessionById(long id);

}