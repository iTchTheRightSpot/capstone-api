package com.example.sarabrandserver.user.repository;

import com.example.sarabrandserver.user.entity.Clientz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepo extends JpaRepository<Clientz, Long> {
    @Query(value = "SELECT c FROM Clientz c WHERE c.email = :principal OR c.username = :principal")
    Optional<Clientz> findByPrincipal(@Param(value = "principal") String principal);

    @Query(value = """
    SELECT COUNT (c.clientId)
    FROM Clientz c
    WHERE c.email = :email OR c.username = :username
    """)
    int principalExists(@Param(value = "email") String email, @Param(value = "username") String username);

    @Query(value = """
    SELECT c
    FROM Clientz c
    WHERE c.email = :email OR c.username = :username
    """)
    Optional<Clientz> workerExists(@Param(value = "email") String email, @Param(value = "username") String username);

    @Query(value = "UPDATE Clientz c SET c.locked = :bool WHERE c.clientId = :id")
    void lockClientz(@Param(value = "bool") boolean bool, @Param(value = "id") Long id);

}
