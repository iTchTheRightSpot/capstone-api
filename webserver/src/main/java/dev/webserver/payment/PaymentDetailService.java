package dev.webserver.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.webserver.cart.CartItemRepository;
import dev.webserver.enumeration.PaymentStatus;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.exception.CustomServerError;
import dev.webserver.user.SarreBrandUserService;
import dev.webserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PaymentDetailService {

    static final Logger log = LoggerFactory.getLogger(PaymentDetailService.class);

    private final PaymentDetailRepository paymentDetailRepository;
    private final SarreBrandUserService userService;
    private final AddressRepository addressRepository;
    private final OrderReservationRepository orderReservationRepository;
    private final PaymentAuthorizationRepository paymentAuthorizationRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderDetailRepository orderDetailRepository;

    /**
     * Retrieves a {@link PaymentDetail} based on its indexed properties.
     *
     * @param email     an index column representing the customers email.
     * @param reference a unique index column representing the details of the
     *                  customers payment. Its is also unique payment detail
     *                  from the 3rd party service.
     * @return true is {@link PaymentDetail} exists else false.
     */
    public boolean paymentDetailExists(final String email, final String reference) {
        return paymentDetailRepository.paymentDetailByEmailAndReference(email, reference).isPresent();
    }

    /**
     * Constructs a {@link PaymentDetail} after a successful payment.
     *
     * @param data contains details of a successful payment.
     */
    public void onSuccessfulPayment(final JsonNode data) {
        // TODO only process in production
        final String domain = data.get("domain").textValue();

        var mapper = new ObjectMapper();

        try {
            final String reference = data.get("reference").textValue();

            var amount = WebHookUtil
                    .fromNumberToBigDecimal(mapper.treeToValue(data.get("amount"), Number.class));

            var metadata = mapper.treeToValue(data.get("metadata"), WebhookMetaData.class);
            var webAuth = mapper
                    .treeToValue(data.get("authorization"), WebhookAuthorization.class);

            var detail = paymentDetail(data, metadata, reference, amount);
            address(metadata, detail);
            paymentAuthorization(webAuth, detail);
            orderDetail(detail, reference);

        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new CustomServerError("error saving a PaymentDetail");
        }
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
    private PaymentDetail paymentDetail(
            final JsonNode data,
            final WebhookMetaData metadata,
            final String reference,
            final BigDecimal amount
    ) {
        // find user
        var user = userService.userByPrincipal(metadata.principal()).orElse(null);

        var currency = SarreCurrency.valueOf(data.get("currency").textValue().toUpperCase());

        // save PaymentDetail
        return paymentDetailRepository.save(
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
    private void address(final WebhookMetaData metadata, final PaymentDetail detail) {
        addressRepository.save(new Address(
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
     * @param auth   The metadata extracted from the webhook containing payment information.
     * @param detail an associated property of an {@link PaymentAuthorization}.
     */
    private void paymentAuthorization(final WebhookAuthorization auth, final PaymentDetail detail) {
        paymentAuthorizationRepository.save(
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
     * Saves {@link OrderDetail}s and deletes the items currently bought in the users cart and
     * {@link OrderReservation}.
     *
     * @param detail    The {@link PaymentDetail} associated with the order.
     * @param reference The reference id associated to an {@link OrderReservation}.
     */
    private void orderDetail(final PaymentDetail detail, final String reference) {
        final var reservations = orderReservationRepository.allReservationsByReference(reference);

        // save OrderDetails
        reservations.forEach(obj -> orderDetailRepository
                .saveOrderDetail(obj.getReservationQty(), obj.getProductSkuId(), detail.getPaymentDetailId()));

        // delete CartItems
        cartItemRepository.cartItemsByOrderReservationReference(reference)
                .forEach(cartItem -> cartItemRepository.deleteById(cartItem.getCartItemId()));

        // delete OrderReservations
        reservations
                .forEach(o -> orderReservationRepository.deleteById(o.getReservationId()));
    }

}
