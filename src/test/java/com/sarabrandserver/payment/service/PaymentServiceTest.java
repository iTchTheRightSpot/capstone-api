package com.sarabrandserver.payment.service;

import com.sarabrandserver.AbstractUnitTest;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.payment.repository.OrderReservationRepo;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.shipping.repository.ShippingRepo;
import com.sarabrandserver.thirdparty.ThirdPartyPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class PaymentServiceTest extends AbstractUnitTest {

    private PaymentService paymentService;

    @Mock
    private ProductSkuRepo skuRepo;
    @Mock
    private ShoppingSessionRepo sessionRepo;
    @Mock
    private CartItemRepo cartItemRepo;
    @Mock
    private OrderReservationRepo reservationRepo;
    @Mock
    private ThirdPartyPaymentService thirdPartyPaymentService;
    @Mock
    private ShippingRepo shippingRepo;

    @BeforeEach
    void setUp() {
//        paymentService = new PaymentService();
    }

    @Test
    void raceCondition() {
    }

    @Test
    void onPendingReservationsNotEmpty() {
    }

    @Test
    void order() {
    }

}