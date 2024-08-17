package dev.webserver.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import dev.webserver.AbstractRepositoryTest;
import dev.webserver.RepositoryTestData;
import dev.webserver.TestUtility;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.enumeration.CapstoneCurrency;
import dev.webserver.enumeration.PaymentStatus;
import dev.webserver.product.*;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class OrderDetailRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDetailRepository detailRepo;
    @Autowired
    private ProductPriceCurrencyRepository currencyRepository;
    @Autowired
    private ProductImageRepository imageRepo;
    @Autowired
    private ProductSkuRepository skuRepo;
    @Autowired
    private PaymentDetailRepository paymentDetailRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private AddressRepository addressRepository;

    @Test
    void orderHistoryByPrincipal() throws JsonProcessingException {
        // given
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepo, currencyRepository, imageRepo, skuRepo);

        final var detail = paymentDetailRepository
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
                detail.paymentId(),
                new Faker().address().streetAddress(),
                new Faker().address().city(),
                new Faker().address().state(),
                new Faker().address().zipCode(),
                new Faker().address().country(),
                new Faker().lorem().characters(500))
        );

        // when
        final var skus = TestUtility.toList(skuRepo.findAll());
        assertFalse(skus.isEmpty());
        final var sku = skus.getFirst();

        orderDetailRepository.save(new OrderDetail(null, sku.inventory(), sku.skuId(), detail.paymentId()));

        // then
        final var details = orderDetailRepository.orderHistoryByPrincipal("hello@hello.com");

        assertFalse(details.isEmpty());

        for (final OrderDetailDbMapper pojo : details) {
            assertNotNull(pojo.createdAt());
            assertNotNull(pojo.currency());
            assertNotNull(pojo.amount());
            assertNotNull(pojo.referenceId());

            final OrderHistoryDbMapper[] arr = new ObjectMapper().readValue(pojo.detail(), OrderHistoryDbMapper[].class);
            assertNotNull(arr);

            for (final OrderHistoryDbMapper mapper : arr) {
                assertNotNull(mapper.name());
                assertNotNull(mapper.colour());
                assertNotNull(mapper.imageKey());
            }
        }
    }

    @Test
    void shouldSuccessfullySaveOrderDetail() {
        final var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepo, currencyRepository, imageRepo, skuRepo);

        final var ldt = CustomUtil.TO_GREENWICH.apply(null);
        final var paymentDetail = paymentDetailRepository.save(
                PaymentDetail.builder()
                        .fullname(new Faker().name().fullName())
                        .email(new Faker().internet().emailAddress())
                        .phone(new Faker().phoneNumber().cellPhone())
                        .referenceId("unique-payment-categoryId")
                        .currency(CapstoneCurrency.USD)
                        .paymentStatus(PaymentStatus.CONFIRMED)
                        .amount(new BigDecimal("50.65"))
                        .paymentProvider("Paystack")
                        .createAt(ldt)
                        .build());

        addressRepository.save(new Address(
                paymentDetail.paymentId(),
                new Faker().address().streetAddress(),
                new Faker().address().city(),
                new Faker().address().state(),
                new Faker().address().zipCode(),
                new Faker().address().country(),
                new Faker().lorem().characters(900)));

        // when
        final var skus = TestUtility.toList(skuRepo.findAll());
        assertFalse(skus.isEmpty());
        final var sku = skus.getFirst();

        // method to test
        orderDetailRepository.saveOrderDetail(sku.inventory(), sku.skuId(), paymentDetail.paymentId());

        // then
        assertFalse(TestUtility.toList(orderDetailRepository.findAll()).isEmpty());
    }

}