package com.example.sarabrandserver.clientz.repository;

import com.example.sarabrandserver.clientz.entity.Clientz;
import com.example.sarabrandserver.enumeration.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Clientz, Long> {
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

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE Clientz c SET c.accountNoneLocked = ?1 WHERE c.clientId = ?2")
    void lockClientAccount(@Param(value = "bool") boolean bool, @Param(value = "id") Long id);

    @Query(value = """
    SELECT COUNT(c.clientId) FROM Clientz c
    JOIN ClientRole r ON c.clientId = r.clientz.clientId
    WHERE (c.email = :email OR c.username = :username)
    AND r.role = :role
    """)
    int isAdmin(
            @Param(value = "email") String email,
            @Param(value = "username") String username,
            @Param(value = "role") RoleEnum role
    );

}
