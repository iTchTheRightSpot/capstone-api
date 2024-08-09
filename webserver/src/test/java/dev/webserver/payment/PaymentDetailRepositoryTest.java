package dev.webserver.payment;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.enumeration.PaymentStatus;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentDetailRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private PaymentDetailRepository repository;

    @Test
    void shouldSuccessfullyRetrieveAPaymentDetailByEmailAndReference() {
        // given
        final var ldt = CustomUtil.TO_GREENWICH.apply(null);
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
                        .paidAt(ldt.format(DateTimeFormatter.ISO_DATE_TIME))
                        .createAt(ldt)
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