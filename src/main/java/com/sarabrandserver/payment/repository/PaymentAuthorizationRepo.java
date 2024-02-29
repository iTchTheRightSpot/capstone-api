package com.sarabrandserver.payment.repository;

import com.sarabrandserver.payment.entity.PaymentAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentAuthorizationRepo extends JpaRepository<PaymentAuthorization, Long> {}