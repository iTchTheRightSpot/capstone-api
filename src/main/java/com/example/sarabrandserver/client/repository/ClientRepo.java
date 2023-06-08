package com.example.sarabrandserver.client.repository;

import com.example.sarabrandserver.client.entity.Clientz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepo extends JpaRepository<Clientz, Long> {
    @Query("SELECT c FROM Clientz c WHERE c.email = :principal")
    Optional<Clientz> findByPrincipal(@Param(value = "principal") String principal);

    @Query("SELECT COUNT (c.clientId) FROM Clientz c WHERE c.email = :email")
    int principalExists(@Param(value = "email") String email);
}
