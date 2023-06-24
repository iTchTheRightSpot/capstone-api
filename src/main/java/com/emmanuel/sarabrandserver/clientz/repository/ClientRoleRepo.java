package com.emmanuel.sarabrandserver.clientz.repository;

import com.emmanuel.sarabrandserver.clientz.entity.ClientRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRoleRepo extends JpaRepository<ClientRole, Long> {}
