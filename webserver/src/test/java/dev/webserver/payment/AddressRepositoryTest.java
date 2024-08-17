package dev.webserver.payment;

import com.github.javafaker.Faker;
import dev.webserver.AbstractRepositoryTest;
import dev.webserver.enumeration.PaymentStatus;
import dev.webserver.enumeration.CapstoneCurrency;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

final class AddressRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private PaymentDetailRepository paymentDetailRepository;
    @Autowired
    private AddressRepository addressRepository;

    @Test
    void save() {
        final var paymentDetail = paymentDetailRepository
                .save(PaymentDetail.builder()
                        .fullname(new Faker().name().fullName())
                        .email("hello@hello.com")
                        .phone("0000000000")
                        .referenceId("unique-payment-categoryId")
                        .currency(CapstoneCurrency.NGN)
                        .paymentStatus(PaymentStatus.CONFIRMED)
                        .amount(new BigDecimal("50.65"))
                        .paymentProvider("Paystack")
                        .createAt(CustomUtil.TO_GREENWICH.apply(null))
                        .build());

        // method to test
        final var address = addressRepository.save(new Address(
                paymentDetail.paymentId(),
                new Faker().address().streetAddress(),
                new Faker().address().city(),
                new Faker().address().state(),
                new Faker().address().zipCode(),
                new Faker().address().country(),
                new Faker().lorem().characters(500))
        );

        // assert
        assertThat(address).isEqualTo(addressRepository.findAll().getFirst());
    }

    @Test
    void findAll() {
        final var paymentDetail = paymentDetailRepository
                .save(PaymentDetail.builder()
                        .fullname(new Faker().name().fullName())
                        .email("hello@hello.com")
                        .phone("0000000000")
                        .referenceId("unique-payment-categoryId")
                        .currency(CapstoneCurrency.NGN)
                        .paymentStatus(PaymentStatus.CONFIRMED)
                        .amount(new BigDecimal("50.65"))
                        .paymentProvider("Paystack")
                        .createAt(CustomUtil.TO_GREENWICH.apply(null))
                        .build());

        addressRepository.save(new Address(
                paymentDetail.paymentId(),
                new Faker().address().streetAddress(),
                new Faker().address().city(),
                new Faker().address().state(),
                new Faker().address().zipCode(),
                new Faker().address().country(),
                new Faker().lorem().characters(500))
        );


        // method to test and assert
        assertThat(addressRepository.findAll().size()).isEqualTo(1);
    }

}