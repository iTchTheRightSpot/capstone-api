package com.sarabrandserver.payment.repository;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.payment.dto.PayloadMapper;
import com.sarabrandserver.payment.entity.Address;
import com.sarabrandserver.payment.entity.OrderDetail;
import com.sarabrandserver.payment.entity.PaymentDetail;
import com.sarabrandserver.payment.projection.OrderPojo;
import com.sarabrandserver.payment.service.OrderService;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.product.service.WorkerProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
class OrderDetailRepositoryTest extends AbstractRepositoryTest {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private WorkerProductService productService;
    @Autowired
    private ProductSkuRepo skuRepo;
    @Autowired
    private PaymentRepo paymentRepo;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private AddressRepo addressRepo;
    @Autowired
    private S3Service s3Service;

    @Test
    void orderHistoryByPrincipal() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        productService
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

        var paymentDetail = paymentRepo
                .save(PaymentDetail.builder()
                        .name(new Faker().name().fullName())
                        .email("hello@hello.com")
                        .phone("0000000000")
                        .paymentId("unique-payment-categoryId")
                        .currency(SarreCurrency.USD)
                        .amount(new BigDecimal("50.65"))
                        .paymentProvider("Paystack")
                        .createAt(new Date())
                        .orderDetails(new HashSet<>())
                        .build()
                );

        addressRepo
                .save(Address.builder()
                        .address("address boulevard")
                        .city("city")
                        .state("state")
                        .postcode("5n32p0")
                        .country("Transylvania")
                        .paymentDetail(paymentDetail)
                        .deliveryInfo(new Faker().lorem().characters(500))
                        .build()
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

            PayloadMapper[] arr = OrderService
                    .transform(s3Service, BUCKET, pojo.getDetail());
            assertNotNull(arr);

            for (PayloadMapper mapper : arr) {
                assertNotNull(mapper.name());
                assertNotNull(mapper.colour());
                assertNotNull(mapper.key());
            }
        }
    }

}