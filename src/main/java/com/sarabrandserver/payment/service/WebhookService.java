package com.sarabrandserver.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.enumeration.PaymentStatus;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.CustomServerError;
import com.sarabrandserver.payment.entity.*;
import com.sarabrandserver.payment.repository.*;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

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
    private final OrderDetailRepository orderDetailRepository;

    /**
     * Processes a payment received via webhook from Paystack.
     * Reference documentation <a href="https://paystack.com/docs/payments/verify-payments/">...</a>
     *
     * @param req the {@link HttpServletRequest} containing the webhook data.
     * @throws CustomServerError if there is an error parsing the request or an invalid request
     * is received from Paystack.
     */
    @Transactional(rollbackFor = {
            IOException.class, CustomServerError.class, JsonProcessingException.class,
            InvalidKeyException.class
    })
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
                log.info("successfully performed business logic on successful webhook request.");
            } else {
                log.info("failed payment");
            }
        } catch (IOException e) {
            log.error("error parsing request {}", e.getMessage());
            throw new CustomServerError("error parsing request");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("error constructing WebhookConstruct {}", e.getMessage());
            throw new CustomServerError("error constructing WebhookConstruct");
        } catch (CustomServerError e) {
            log.error("error from paystack webhook {}", e.getMessage());
            throw new CustomServerError(e.getMessage());
        }
    }

    /**
     * Processes the webhook data when a payment is successful.
     *
     * @param data The {@link JsonNode} containing the webhook data.
     * @throws JsonProcessingException if there is an error occurs transforming data to a custom object.
     */
    void onSuccessWebHook(JsonNode data) throws JsonProcessingException {
        // TODO only process in production
        String domain = data.get("domain").textValue();

        ObjectMapper mapper = new ObjectMapper();

        BigDecimal amount = WebHookUtil
                .fromNumberToBigDecimal(mapper.treeToValue(data.get("amount"), Number.class));
        String reference = data.get("reference").textValue();
        WebhookMetaData metadata = mapper.treeToValue(data.get("metadata"), WebhookMetaData.class);
        WebhookAuthorization webAuth = mapper
                .treeToValue(data.get("authorization"), WebhookAuthorization.class);

        PaymentDetail detail = paymentDetail(data, metadata, reference, amount);
        address(metadata, detail);
        paymentAuthorization(webAuth, detail);
        processPaymentOrderReservations(detail, reference.substring(4));
    }

    /**
     * Creates and saves a {@link PaymentDetail} object based on the webhook metadata, transaction data,
     * and user information.
     *
     * @param data      The {@link JsonNode} containing transaction data.
     * @param metadata  The metadata extracted from the webhook containing customer information.
     * @param reference The reference ID associated with the payment.
     * @param amount    The amount of the payment.
     * @return The saved {@link PaymentDetail} object.
     */
    private PaymentDetail paymentDetail(JsonNode data, WebhookMetaData metadata, String reference, BigDecimal amount) {
        // find user
        var user = userService.userByPrincipal(metadata.principal()).orElse(null);

        SarreCurrency currency = SarreCurrency.valueOf(data.get("currency").textValue().toUpperCase());

        // save PaymentDetail
        return paymentDetailRepo.save(
                PaymentDetail.builder()
                        .name(metadata.name())
                        .email(metadata.email())
                        .phone(metadata.phone())
                        .referenceId(reference)
                        .currency(currency)
                        .amount(WebHookUtil.fromLowestCurrencyFormToCurrency(amount, currency))
                        .paymentProvider("Paystack")
                        .paymentStatus(PaymentStatus.CONFIRMED)
                        .paidAt(data.get("paid_at").textValue())
                        .createAt(CustomUtil.toUTC(new Date()))
                        .user(user)
                        .orderDetails(new HashSet<>())
                        .build()
        );
    }

    /**
     * Saves an {@link Address} object based on the {@link WebhookMetaData} and {@link PaymentDetail}.
     *
     * @param metadata The metadata extracted from the webhook containing customer information.
     * @param detail   an associated property of an {@link Address}.
     */
    private void address(WebhookMetaData metadata, PaymentDetail detail) {
        addressRepo.save(new Address(
                metadata.address(),
                metadata.city(),
                metadata.state(),
                metadata.postcode(),
                metadata.country(),
                metadata.deliveryInfo(),
                detail)
        );
    }

    /**
     * Saves an {@link PaymentAuthorization} object based on the {@link WebhookAuthorization} and
     * {@link PaymentDetail}.
     *
     * @param auth  The metadata extracted from the webhook containing payment information.
     * @param detail an associated property of an {@link PaymentAuthorization}.
     */
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

    /**
     * After processing a {@link PaymentDetail}, we create process the {@link OrderDetail} by
     * retrieving all of PENDING {@link OrderReservation}s by the reference. After which we
     * delete all {@link OrderReservation}s and {@link com.sarabrandserver.cart.entity.CartItem}s
     * by {@link com.sarabrandserver.cart.entity.ShoppingSession}.
     *
     * @param detail    The {@link PaymentDetail} associated with the order.
     * @param reference The reference id associated to an {@link OrderReservation}.
     */
    private void processPaymentOrderReservations(PaymentDetail detail, String reference) {
        List<OrderReservation> reservations = orderReservationRepo.allReservationsByReference(reference);

        // save OrderDetails
        reservations.stream()
                .map(o -> new OrderDetail(o.getQty(), o.getProductSku(), detail))
                .forEach(orderDetailRepository::save);

        // delete CartItems with the same ProductSku
        reservations.stream().map(OrderReservation::getProductSku)
                .toList()
                .stream()
                .flatMap(s -> cartItemRepo
                        .cartItemsByShoppingSessionId(reservations.getFirst().getShoppingSession().shoppingSessionId())
                        .stream()
                        .filter(c -> Objects.equals(c.getProductSku().getSku(), s.getSku()))
                )
                .forEach(cartItem -> cartItemRepo.deleteCartItemByCartItemId(cartItem.getCartId()));

        // delete OrderReservations
        reservations.forEach(o -> orderReservationRepo.deleteOrderReservationByReservationId(o.getReservationId()));
    }

}
