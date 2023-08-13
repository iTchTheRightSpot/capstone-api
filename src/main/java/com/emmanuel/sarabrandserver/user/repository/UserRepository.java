package com.emmanuel.sarabrandserver.user.repository;

import com.emmanuel.sarabrandserver.user.entity.SaraBrandUser;
import com.emmanuel.sarabrandserver.user.projection.ClientzPojo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<SaraBrandUser, Long> {
    @Query(value = "SELECT c FROM SaraBrandUser c WHERE c.email = :principal")
    Optional<SaraBrandUser> findByPrincipal(@Param(value = "principal") String principal);

    @Query(value = """
    SELECT COUNT (c.clientId)
    FROM SaraBrandUser c
    WHERE c.email = :email
    """)
    int principalExists(@Param(value = "email") String email);

    @Query(value = """
    SELECT c
    FROM SaraBrandUser c
    WHERE c.email = :email
    """)
    Optional<SaraBrandUser> workerExists(@Param(value = "email") String email);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE SaraBrandUser c SET c.accountNoneLocked = :bool WHERE c.clientId = :id")
    void lockClientAccount(@Param(value = "bool") boolean bool, @Param(value = "id") long id);

    @Query(value = """
    SELECT c.firstname AS name FROM SaraBrandUser c
    INNER JOIN ClientRole r ON c.clientId = r.saraBrandUser.clientId
    WHERE r.role = :role
    """)
    Page<ClientzPojo> fetchAll(@Param(value = "role") String role, Pageable page);
}
