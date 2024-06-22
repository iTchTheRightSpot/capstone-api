package dev.webserver.payment.repository;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.enumeration.PaymentStatus;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.payment.entity.PaymentDetail;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentDetailRepoTest extends AbstractRepositoryTest {

    @Autowired
    private PaymentDetailRepo repository;

    @Test
    void shouldSuccessfullyRetrieveAPaymentDetailByEmailAndReference() {
        // given
        repository.save(
                PaymentDetail.builder()
                        .name("Bane Anderson")
                        .email("baneanderson@email.com")
                        .phone("0000000000")
                        .referenceId("ref-bane")
                        .currency(SarreCurrency.NGN)
                        .amount(new BigDecimal("15750"))
                        .paymentProvider("Paystack")
                        .paymentStatus(PaymentStatus.CONFIRMED)
                        .paidAt(new Date().toString())
                        .createAt(CustomUtil.toUTC(new Date()))
                        .orderDetails(new HashSet<>())
                        .build()
        );

        // when
        var optional = repository
                .paymentDetailByEmailAndReference("baneanderson@email.com", "ref-bane");

        // then
        assertFalse(optional.isEmpty());
    }

    @Test
    void shouldNotSuccessfullyRetrieveAPaymentDetailByEmailAndReference() {
        // when
        var optional = repository
                .paymentDetailByEmailAndReference("baneanderson@email.com", "ref-bane");

        // then
        assertTrue(optional.isEmpty());
    }

}