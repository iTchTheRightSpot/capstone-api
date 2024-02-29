package com.sarabrandserver.payment.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.RepositoryTestData;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.payment.dto.PayloadMapper;
import com.sarabrandserver.payment.entity.Address;
import com.sarabrandserver.payment.entity.OrderDetail;
import com.sarabrandserver.payment.entity.PaymentDetail;
import com.sarabrandserver.payment.projection.OrderPojo;
import com.sarabrandserver.product.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
class OrderDetailRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductDetailRepo detailRepo;
    @Autowired
    private PriceCurrencyRepo priceCurrencyRepo;
    @Autowired
    private ProductImageRepo imageRepo;
    @Autowired
    private ProductSkuRepo skuRepo;
    @Autowired
    private PaymentDetailRepo paymentDetailRepo;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private AddressRepo addressRepo;

    @Test
    void orderHistoryByPrincipal() throws JsonProcessingException {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        RepositoryTestData
                .createProduct(2, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        var paymentDetail = paymentDetailRepo
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

        addressRepo.save(new Address(
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

        orderDetailRepository
                .save(new OrderDetail(sku.getInventory(), sku, paymentDetail));

        // then
        var details = orderDetailRepository
                .orderHistoryByPrincipal("hello@hello.com");

        assertFalse(details.isEmpty());

        for (OrderPojo pojo : details) {
            assertNotNull(pojo.getTime());
            assertNotNull(pojo.getCurrency());
            assertNotNull(pojo.getTotal());
            assertNotNull(pojo.getPaymentId());

            PayloadMapper[] arr = new ObjectMapper().readValue(pojo.getDetail(), PayloadMapper[].class);
            assertNotNull(arr);

            for (PayloadMapper mapper : arr) {
                assertNotNull(mapper.name());
                assertNotNull(mapper.colour());
                assertNotNull(mapper.key());
            }
        }
    }

}