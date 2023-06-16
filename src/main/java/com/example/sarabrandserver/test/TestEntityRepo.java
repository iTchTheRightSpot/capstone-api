package com.example.sarabrandserver.test;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestEntityRepo extends JpaRepository<TestEntity, Long> {}
