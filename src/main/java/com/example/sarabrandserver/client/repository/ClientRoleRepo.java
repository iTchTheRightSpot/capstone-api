package com.example.sarabrandserver.client.repository;

import com.example.sarabrandserver.client.entity.ClientRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRoleRepo extends JpaRepository<ClientRole, Long> {}
