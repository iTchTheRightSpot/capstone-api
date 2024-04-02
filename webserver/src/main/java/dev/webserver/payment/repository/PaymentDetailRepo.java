package dev.webserver.payment.repository;

import dev.webserver.payment.entity.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentDetailRepo extends JpaRepository<PaymentDetail, Long> {

    @Query("SELECT p FROM PaymentDetail p WHERE p.email = :email AND p.referenceId = :reference")
    Optional<PaymentDetail> paymentDetailByEmailAndReference(String email, String reference);

}