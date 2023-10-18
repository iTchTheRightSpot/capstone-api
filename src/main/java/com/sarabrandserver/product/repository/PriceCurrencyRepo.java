package com.sarabrandserver.product.repository;

import com.sarabrandserver.product.entity.PriceCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceCurrencyRepo extends JpaRepository<PriceCurrency, Long> { }