package dev.webserver.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import dev.webserver.AbstractRepositoryTest;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;

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
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        var paymentDetail = paymentDetailRepository
                .save(PaymentDetail.builder()
                        .name(new Faker().name().fullName())
                        .email("hello@hello.com")
                        .phone("0000000000")
                        .referenceId("unique-payment-categoryId")
                        .currency(SarreCurrency.USD)
                        .amount(new BigDecimal("50.65"))
                        .paymentProvider("Paystack")
                        .createAt(new Date())
                        .orderDetails(new HashSet<>())
                        .build()
                );

        addressRepository.save(new Address(
                "address boulevard",
                "city",
                "state",
                "postcode",
                "Transylvania",
                new Faker().lorem().characters(500),
                paymentDetail)
        );


        // when
        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());
        var sku = skus.getFirst();

        orderDetailRepository.save(new OrderDetail(sku.getInventory(), sku, paymentDetail));

        // then
        var details = orderDetailRepository.orderHistoryByPrincipal("hello@hello.com");

        assertFalse(details.isEmpty());

        for (OrderDetailDbMapper pojo : details) {
            assertNotNull(pojo.getTime());
            assertNotNull(pojo.getCurrency());
            assertNotNull(pojo.getTotal());
            assertNotNull(pojo.getPaymentId());

            OrderHistoryDbMapper[] arr = new ObjectMapper().readValue(pojo.getDetail(), OrderHistoryDbMapper[].class);
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
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        RepositoryTestData
                .createProduct(2, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        var paymentDetail = paymentDetailRepository
                .save(PaymentDetail.builder()
                        .name(new Faker().name().fullName())
                        .email("hello@hello.com")
                        .phone("0000000000")
                        .referenceId("unique-payment-categoryId")
                        .currency(SarreCurrency.USD)
                        .amount(new BigDecimal("50.65"))
                        .paymentProvider("Paystack")
                        .createAt(new Date())
                        .orderDetails(new HashSet<>())
                        .build()
                );

        addressRepository.save(new Address(
                "address boulevard",
                "city",
                "state",
                "postcode",
                "Transylvania",
                new Faker().lorem().characters(500),
                paymentDetail)
        );


        // when
        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());
        var sku = skus.getFirst();

        // method to test
        orderDetailRepository
                .saveOrderDetail(sku.getInventory(), sku.getSkuId(), paymentDetail.getPaymentDetailId());

        // then
        assertFalse(orderDetailRepository.findAll().isEmpty());
    }

}