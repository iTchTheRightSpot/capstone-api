package com.sarabrandserver.order.repository;

import com.sarabrandserver.order.entity.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepo extends JpaRepository<PaymentDetail, Long> { }