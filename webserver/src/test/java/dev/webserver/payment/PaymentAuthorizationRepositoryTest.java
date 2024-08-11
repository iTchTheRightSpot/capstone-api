package dev.webserver.payment;

import com.github.javafaker.Faker;
import dev.webserver.AbstractRepositoryTest;
import dev.webserver.enumeration.PaymentStatus;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

final class PaymentAuthorizationRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private PaymentDetailRepository paymentDetailRepository;
    @Autowired
    private PaymentAuthorizationRepository authorizationRepository;

    @Test
    void save() {
        // given
        final var paymentDetail = paymentDetailRepository
                .save(PaymentDetail.builder()
                        .fullname(new Faker().name().fullName())
                        .email("hello@hello.com")
                        .phone("0000000000")
                        .referenceId("unique-payment-categoryId")
                        .currency(SarreCurrency.NGN)
                        .paymentStatus(PaymentStatus.CONFIRMED)
                        .amount(new BigDecimal("50.65"))
                        .paymentProvider("Paystack")
                        .createAt(CustomUtil.TO_GREENWICH.apply(null))
                        .build());

        // method to test
        final var authorization = authorizationRepository.save(
                PaymentAuthorization.builder()
                        .authorizationId(paymentDetail.paymentId())
                        .authorizationCode(new Faker().lorem().characters(20))
                        .bin(new Faker().lorem().characters(20))
                        .last4("20044")
                        .expirationMonth("08")
                        .expirationYear("025263")
                        .channel(new Faker().lorem().characters(10))
                        .cardType(new Faker().lorem().characters(20))
                        .bank(new Faker().lorem().characters(100))
                        .countryCode(new Faker().lorem().characters(10))
                        .brand(new Faker().lorem().characters(20))
                        .isReusable(true)
                        .signature(new Faker().lorem().characters(50))
                        .build());

        // assert
        assertThat(authorization).isEqualTo(authorizationRepository.findAll().getFirst());
    }

    @Test
    void findAll() {
        // given
        final var paymentDetail = paymentDetailRepository
                .save(PaymentDetail.builder()
                        .fullname(new Faker().name().fullName())
                        .email("hello@hello.com")
                        .phone("0000000000")
                        .referenceId("unique-payment-categoryId")
                        .currency(SarreCurrency.NGN)
                        .paymentStatus(PaymentStatus.CONFIRMED)
                        .amount(new BigDecimal("50.65"))
                        .paymentProvider("Paystack")
                        .createAt(CustomUtil.TO_GREENWICH.apply(null))
                        .build());

        // method to test
        authorizationRepository.save(
                PaymentAuthorization.builder()
                        .authorizationId(paymentDetail.paymentId())
                        .authorizationCode(new Faker().lorem().characters(20))
                        .bin(new Faker().lorem().characters(20))
                        .last4("20044")
                        .expirationMonth("08")
                        .expirationYear("025263")
                        .channel(new Faker().lorem().characters(10))
                        .cardType(new Faker().lorem().characters(20))
                        .bank(new Faker().lorem().characters(100))
                        .countryCode(new Faker().lorem().characters(10))
                        .brand(new Faker().lorem().characters(20))
                        .isReusable(true)
                        .signature(new Faker().lorem().characters(50))
                        .build());

        // assert
        assertThat(authorizationRepository.findAll().size()).isEqualTo(1);
    }
}