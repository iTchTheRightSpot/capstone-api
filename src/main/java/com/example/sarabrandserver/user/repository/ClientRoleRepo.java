package com.example.sarabrandserver.user.repository;

import com.example.sarabrandserver.user.entity.ClientRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRoleRepo extends JpaRepository<ClientRole, Long> {}
