package dev.webserver.payment;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PaymentDetailRepository extends CrudRepository<PaymentDetail, Long> {
    @Query("SELECT * FROM payment_detail  WHERE email = :email AND reference_id = :reference")
    Optional<PaymentDetail> paymentDetailByEmailAndReference(String email, String reference);
}