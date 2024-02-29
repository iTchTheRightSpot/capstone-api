package com.sarabrandserver.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.enumeration.PaymentStatus;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.CustomServerError;
import com.sarabrandserver.payment.entity.Address;
import com.sarabrandserver.payment.entity.OrderReservation;
import com.sarabrandserver.payment.entity.PaymentAuthorization;
import com.sarabrandserver.payment.entity.PaymentDetail;
import com.sarabrandserver.payment.repository.AddressRepo;
import com.sarabrandserver.payment.repository.OrderReservationRepo;
import com.sarabrandserver.payment.repository.PaymentAuthorizationRepo;
import com.sarabrandserver.payment.repository.PaymentDetailRepo;
import com.sarabrandserver.payment.util.WebHookUtil;
import com.sarabrandserver.payment.util.WebhookAuthorization;
import com.sarabrandserver.payment.util.WebhookConstruct;
import com.sarabrandserver.payment.util.WebhookMetaData;
import com.sarabrandserver.thirdparty.ThirdPartyPaymentService;
import com.sarabrandserver.user.service.SarreBrandUserService;
import com.sarabrandserver.util.CustomUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final SarreBrandUserService userService;
    private final ThirdPartyPaymentService thirdPartyService;
    private final AddressRepo addressRepo;
    private final PaymentDetailRepo paymentDetailRepo;
    private final PaymentAuthorizationRepo paymentAuthorizationRepo;
    private final OrderReservationRepo orderReservationRepo;
    private final CartItemRepo cartItemRepo;

    /**
     * Processes a payment received via webhook
     * <a href="https://paystack.com/docs/payments/verify-payments/">...</a>
     */
    @Transactional
    public void webhook(HttpServletRequest req) {
        try {
            log.info("webhook received");
            String body = WebHookUtil.httpServletRequestToString(req);

            WebhookConstruct pair = WebHookUtil
                    .validateRequestFromPayStack(thirdPartyService.payStackCredentials().secretKey(), body);

            if (!pair.validate().toLowerCase().equals(req.getHeader("x-paystack-signature"))) {
                log.error("invalid request from paystack");
                throw new CustomServerError("invalid webhook from paystack");
            }

            if (pair.node().get("event").textValue().equals("charge.success")) {
                onSuccessWebHook(pair.node().get("data"));
            }
        } catch (IOException e) {
            log.error("error parsing request {}", e.getMessage());
            throw new CustomServerError("error parsing request");
        } catch (CustomServerError e) {
            log.error("error from paystack webhook {}", e.getMessage());
            throw new CustomServerError(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("error converting to custom object {}", e.getMessage());
            throw new CustomServerError(e.getMessage());
        }
    }

    @Transactional
    void onSuccessWebHook(JsonNode data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        String domain = data.get("domain").textValue();
        BigDecimal amount = WebHookUtil
                .fromNumberToBigDecimal(mapper.treeToValue(data.get("amount"), Number.class));
        String reference = data.get("reference").textValue();
        WebhookMetaData metadata = mapper.treeToValue(data.get("metadata"), WebhookMetaData.class);
        WebhookAuthorization webAuth = mapper
                .treeToValue(data.get("authorization"), WebhookAuthorization.class);

        PaymentDetail detail = paymentDetail(data, metadata, reference, amount);
        address(metadata, detail);
        paymentAuthorization(webAuth, detail);

        // TODO
        orderReservations(reference.substring(4));

    }

    private PaymentDetail paymentDetail(JsonNode data, WebhookMetaData metadata, String reference, BigDecimal amount) {
        // find user
        var user = userService.userByPrincipal(metadata.principal()).orElse(null);

        // save PaymentDetail
        return paymentDetailRepo.save(
                PaymentDetail.builder()
                        .name(metadata.name())
                        .email(metadata.email())
                        .phone(metadata.phone())
                        .referenceId(reference)
                        .currency(SarreCurrency.valueOf(data.get("currency").textValue().toUpperCase()))
                        .amount(amount) // TODO convert bach to currency
                        .paymentProvider("Paystack")
                        .paymentStatus(PaymentStatus.CONFIRMED)
                        .paidAt(data.get("paid_at").textValue())
                        .createAt(CustomUtil.toUTC(new Date()))
                        .user(user)
                        .orderDetails(new HashSet<>())
                        .build()
        );
    }

    private void address(WebhookMetaData d, PaymentDetail detail) {
        addressRepo.save(new Address(
                d.address(),
                d.city(),
                d.state(),
                d.postcode(),
                d.country(),
                d.deliveryInfo(),
                detail)
        );
    }

    private void paymentAuthorization(WebhookAuthorization auth, PaymentDetail detail) {
        paymentAuthorizationRepo.save(
                PaymentAuthorization.builder()
                        .authorizationCode(auth.authorization_code())
                        .bin(auth.bin())
                        .last4(auth.last4())
                        .expirationMonth(auth.exp_month())
                        .expirationYear(auth.exp_year())
                        .channel(auth.channel())
                        .cardType(auth.card_type())
                        .bank(auth.bank())
                        .countryCode(auth.country_code())
                        .brand(auth.brand())
                        .isReusable(auth.reusable())
                        .signature(auth.signature())
                        .paymentDetail(detail)
                        .build()
        );
    }

    private void orderReservations(String reference) {
        List<OrderReservation> list = orderReservationRepo.allReservationsByReference(reference);
        ShoppingSession session = list.getFirst().getShoppingSession();

        // delete all from cart that reservation equal
        List<CartItem> carts = cartItemRepo.cartItemsByShoppingSessionId(session.shoppingSessionId());

    }

}
