package com.sarabrandserver.payment.repository;

import com.sarabrandserver.payment.entity.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepo extends JpaRepository<PaymentDetail, Long> { }