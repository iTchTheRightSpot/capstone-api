package dev.webserver.payment;

import org.springframework.data.repository.CrudRepository;

public interface PaymentAuthorizationRepository extends CrudRepository<PaymentAuthorization, Long> {}