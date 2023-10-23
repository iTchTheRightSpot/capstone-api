package com.sarabrandserver.cart.repository;

import com.sarabrandserver.cart.entity.ShoppingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
public interface ShoppingSessionRepo extends JpaRepository<ShoppingSession, Long> {

    @Query("SELECT s FROM ShoppingSession s WHERE s.shoppingSessionId = :id")
    Optional<ShoppingSession> shoppingSessionById(Long id);

    @Query(value = """
    UPDATE ShoppingSession s
    SET s.expireAt = :d
    WHERE s.shoppingSessionId = :id
    """)
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void updateSessionExpiry(long id, Date d);

    @Query(value = """
    SELECT s FROM ShoppingSession s
    INNER JOIN SarreBrandUser u ON s.sarreBrandUser.clientId = u.clientId
    WHERE u.email = :principal
    """)
    Optional<ShoppingSession> shoppingSessionByPrincipal(String principal);

}