package dev.webserver.payment;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PaymentDetailRepository extends CrudRepository<PaymentDetail, Long> {
    @Query("SELECT p FROM payment_detail p WHERE p.email = :email AND p.referenceId = :reference")
    Optional<PaymentDetail> paymentDetailByEmailAndReference(String email, String reference);
}