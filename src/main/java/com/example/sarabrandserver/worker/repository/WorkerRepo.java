package com.example.sarabrandserver.worker.repository;

import com.example.sarabrandserver.worker.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkerRepo extends JpaRepository<Worker, Long> {

    @Query("SELECT w FROM Worker w WHERE w.email = :principal OR w.username = :principal")
    Optional<Worker> findByPrincipal(@Param(value = "principal") String principal);

    @Query("SELECT COUNT (w.workerId) FROM Worker w WHERE (w.email = :email OR w.username = :username)")
    int principalExists(@Param(value = "email") String email, @Param(value = "username") String username);

}
