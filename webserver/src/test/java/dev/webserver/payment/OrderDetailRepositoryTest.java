package dev.webserver.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import dev.webserver.AbstractRepositoryTest;
import dev.webserver.TestUtility;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.*;
import dev.webserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderDetailRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDetailRepository detailRepo;
    @Autowired
    private PriceCurrencyRepository priceCurrencyRepository;
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
        var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        final var ldt = CustomUtil.TO_GREENWICH.apply(null);

        var paymentDetail = paymentDetailRepository
                .save(PaymentDetail.builder()
                        .name(new Faker().name().fullName())
                        .email("hello@hello.com")
                        .phone("0000000000")
                        .referenceId("unique-payment-categoryId")
                        .currency(SarreCurrency.USD)
                        .amount(new BigDecimal("50.65"))
                        .paymentProvider("Paystack")
                        .createAt(ldt)
                        .build());

        addressRepository.save(new Address(
                null,
                "address boulevard",
                "city",
                "state",
                "postcode",
                "Transylvania",
                new Faker().lorem().characters(500))
        );


        // when
        var skus = TestUtility.toList(skuRepo.findAll());
        assertFalse(skus.isEmpty());
        var sku = skus.getFirst();

        orderDetailRepository.save(new OrderDetail(null, sku.inventory(), sku.skuId(), paymentDetail.paymentDetailId()));

        // then
        var details = orderDetailRepository.orderHistoryByPrincipal("hello@hello.com");

        assertFalse(details.isEmpty());

        for (OrderDetailDbMapper pojo : details) {
            assertNotNull(pojo.createdAt());
            assertNotNull(pojo.currency());
            assertNotNull(pojo.amount());
            assertNotNull(pojo.referenceId());

            OrderHistoryDbMapper[] arr = new ObjectMapper().readValue(pojo.detail(), OrderHistoryDbMapper[].class);
            assertNotNull(arr);

            for (OrderHistoryDbMapper mapper : arr) {
                assertNotNull(mapper.name());
                assertNotNull(mapper.colour());
                assertNotNull(mapper.imageKey());
            }
        }
    }

    @Test
    void shouldSuccessfullySaveOrderDetail() {
        var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        final var ldt = CustomUtil.TO_GREENWICH.apply(null);
        var paymentDetail = paymentDetailRepository
                .save(PaymentDetail.builder()
                        .name(new Faker().name().fullName())
                        .email("hello@hello.com")
                        .phone("0000000000")
                        .referenceId("unique-payment-categoryId")
                        .currency(SarreCurrency.USD)
                        .amount(new BigDecimal("50.65"))
                        .paymentProvider("Paystack")
                        .createAt(ldt)
                        .build());

        addressRepository.save(new Address(
                null,
                "address boulevard",
                "city",
                "state",
                "postcode",
                "Transylvania",
                new Faker().lorem().characters(500))
        );


        // when
        var skus = TestUtility.toList(skuRepo.findAll());
        assertFalse(skus.isEmpty());
        var sku = skus.getFirst();

        // method to test
        orderDetailRepository
                .saveOrderDetail(sku.inventory(), sku.skuId(), paymentDetail.paymentDetailId());

        // then
        assertFalse(TestUtility.toList(orderDetailRepository.findAll()).isEmpty());
    }

}