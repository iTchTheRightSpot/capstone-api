package com.sarabrandserver.user.repository;

import com.sarabrandserver.user.entity.SarreBrandUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<SarreBrandUser, Long> {

    @Query(value = "SELECT c FROM SarreBrandUser c WHERE c.email = :principal")
    Optional<SarreBrandUser> findByPrincipal(@Param(value = "principal") String principal);

    @Query(value = """
    SELECT COUNT (c.clientId)
    FROM SarreBrandUser c
    WHERE c.email = :email
    """)
    int principalExists(@Param(value = "email") String email);

    @Query(value = """
    SELECT c
    FROM SarreBrandUser c
    WHERE c.email = :email
    """)
    Optional<SarreBrandUser> workerExists(@Param(value = "email") String email);

    @Query(value = "SELECT u FROM SarreBrandUser u")
    Page<SarreBrandUser> allUsers(Pageable page);

}
