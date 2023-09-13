package com.emmanuel.sarabrandserver.user.repository;

import com.emmanuel.sarabrandserver.user.entity.SarreBrandUser;
import com.emmanuel.sarabrandserver.user.projection.ClientzPojo;
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

    @Query(value = """
    SELECT c.firstname AS name FROM SarreBrandUser c
    INNER JOIN ClientRole r ON c.clientId = r.sarreBrandUser.clientId
    WHERE r.role = :role
    """)
    Page<ClientzPojo> fetchAll(@Param(value = "role") String role, Pageable page);
}
