package dev.webserver.user.repository;

import dev.webserver.user.entity.ClientRole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<ClientRole, Long> { }
