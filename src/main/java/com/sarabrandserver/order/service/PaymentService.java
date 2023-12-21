package com.sarabrandserver.order.service;

import com.sarabrandserver.address.Address;
import com.sarabrandserver.address.AddressDTO;
import com.sarabrandserver.address.AddressRepo;
import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.enumeration.PaymentStatus;
import com.sarabrandserver.enumeration.ReservationStatus;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.order.dto.PaymentDTO;
import com.sarabrandserver.order.dto.SkuQtyDTO;
import com.sarabrandserver.order.entity.OrderDetail;
import com.sarabrandserver.order.entity.OrderReservation;
import com.sarabrandserver.order.entity.PaymentDetail;
import com.sarabrandserver.order.repository.OrderRepository;
import com.sarabrandserver.order.repository.OrderReservationRepo;
import com.sarabrandserver.order.repository.PaymentRepo;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.thirdparty.PaymentCredentialObj;
import com.sarabrandserver.thirdparty.ThirdPartyPaymentService;
import com.sarabrandserver.util.CustomUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final long bound = 15;

    @Value("${cart.cookie.name}")
    private String CART_COOKIE;

    private final ProductSkuRepo productSkuRepo;
    private final AddressRepo addressRepo;
    private final PaymentRepo paymentRepo;
    private final OrderRepository orderRepo;
    private final CartItemRepo cartItemRepo;
    private final OrderReservationRepo reservationRepo;
    private final ThirdPartyPaymentService thirdPartyService;
    private final CustomUtil customUtil;

    /**
     * Method helps prevent race condition or overselling by temporarily deducting
     * what is in the users cart with ProductSKU inventory.
     * */
    @Transactional
    public PaymentCredentialObj validate(HttpServletRequest req) {
        Cookie cookie = this.customUtil.cookie.apply(req, CART_COOKIE);

        if (cookie == null) {
            throw new CustomNotFoundException("No cookie found. Kindly refresh window");
        }

        var list = this.cartItemRepo
                .cart_items_by_shopping_session_cookie(cookie.getValue());

        if (list.isEmpty()) {
            throw new CustomNotFoundException("invalid shopping session");
        }

        long toExpire = Instant.now().plus(bound, ChronoUnit.MINUTES).toEpochMilli();
        Date date = this.customUtil.toUTC(new Date(toExpire));

        for (CartItem c : list) {
            this.productSkuRepo.updateInventory(c.getSku(), c.getQty());
            this.reservationRepo
                    .save(new OrderReservation(c.getSku(), c.getQty(), ReservationStatus.PENDING, date));
        }

        var secret = this.thirdPartyService.payStackCredentials();
        return new PaymentCredentialObj(secret.pubKey());
    }

    /**
     * Method retrieves info sent from Flutterwave via webhook
     * */
    @Transactional
    public void order(HttpServletRequest req) {
        try (BufferedReader reader = req.getReader()) {
            reader.lines().forEach(e -> log.info("Buffer reader stream {}", e));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test implementation for purchasing a product
     * */
    @Transactional
    public void test(final PaymentDTO dto, final AddressDTO dto1) {
        for (SkuQtyDTO obj : dto.dto()) {
            this.productSkuRepo.updateInventory(obj.sku(), obj.qty());
        }

        Date date = this.customUtil.toUTC(new Date());

        // currency
        var currency = SarreCurrency.valueOf(dto.currency());

        // save PaymentDetail
        var payment = PaymentDetail.builder()
                .email(dto.email())
                .name(dto.name())
                .phone(dto.phone())
                .payment_id(UUID.randomUUID().toString())
                .currency(currency)
                .amount(dto.total())
                .paymentProvider(dto.paymentProvider())
                .paymentStatus(PaymentStatus.CONFIRMED)
                .createAt(date)
                .orderDetail(new HashSet<>())
                .build();

        var savedPayment = this.paymentRepo.save(payment);

        // save OrderDetail
        for (SkuQtyDTO obj : dto.dto()) {
            this.orderRepo.save(new OrderDetail(obj.sku(), obj.qty(), savedPayment));
        }

        // save Address
        var address = Address.builder()
                .address(dto1.address())
                .city(dto1.city())
                .state(dto1.state())
                .postcode(dto1.postcode())
                .country(dto1.country())
                .deliveryInfo(dto1.deliveryInfo())
                .paymentDetail(savedPayment)
                .build();

        this.addressRepo.save(address);
    }

}