package dev.webserver.payment.repository;

import dev.webserver.payment.entity.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentDetailRepo extends JpaRepository<PaymentDetail, Long> { }