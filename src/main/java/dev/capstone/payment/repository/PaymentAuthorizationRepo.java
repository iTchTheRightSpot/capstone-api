package dev.capstone.payment.repository;

import dev.capstone.payment.entity.PaymentAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentAuthorizationRepo extends JpaRepository<PaymentAuthorization, Long> {}