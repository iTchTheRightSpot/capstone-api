package com.example.sarabrandserver.worker.repository;

import com.example.sarabrandserver.worker.entity.WorkerRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerRoleRepo extends JpaRepository<WorkerRole, Long> {}
