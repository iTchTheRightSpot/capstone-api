package dev.webserver.payment;

import com.fasterxml.jackson.databind.JsonNode;
import dev.webserver.AbstractEnvironment;
import dev.webserver.exception.CustomServerError;
import dev.webserver.external.log.ILogEventPublisher;
import dev.webserver.product.IProductCachePublisher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Transactional(rollbackFor = Exception.class)
class WebhookService extends AbstractEnvironment {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final PaymentDetailService paymentDetailService;
    private final ILogEventPublisher publisher;
    private final IProductCachePublisher productCachePublisher;

    protected WebhookService(
            final Environment environment,
            final PaymentDetailService paymentDetailService,
            final ILogEventPublisher publisher,
            final IProductCachePublisher productCachePublisher
    ) {
        super(environment);
        this.paymentDetailService = paymentDetailService;
        this.publisher = publisher;
        this.productCachePublisher = productCachePublisher;
    }

    /**
     * Processes a payment received via webhook from Paystack.
     * Reference <a href="https://paystack.com/docs/payments/webhooks/">documentation</a>
     *
     * @param req the {@link HttpServletRequest} containing the webhook data.
     * @throws CustomServerError if there is an error parsing the request or an invalid request
     * is received from Paystack.
     */
    public void webhook(final HttpServletRequest req) {
        try {
            log.info("webhook received");
            final String body = WebHookUtil.httpServletRequestToString(req);

            final var pair = WebHookUtil.validateRequestFromPayStack(super.payStackCredentials().secretKey(), body);

            if (!pair.validate().toLowerCase().equals(req.getHeader("x-paystack-signature"))) {
                log.error("invalid request from paystack");
                throw new CustomServerError("invalid webhook from paystack");
            }

            final JsonNode data = pair.node().get("data");
            if (pair.node().get("event").textValue().equals("charge.success") && data.get("status").textValue().equals("success")) {
                final String reference = data.get("reference").textValue();
                final JsonNode metadata = data.get("metadata");
                final String email = metadata.get("email").asText();

                if (paymentDetailService.isPaymentDetailMissingByEmailAndReference(email, reference)) {
                    onSuccessWebHook(data);
                    productCachePublisher.evictAll();
                    publisher.publishPurchase(metadata.get("name").asText(), email);
                    log.info("successfully performed business logic on successful webhook request.");
                } else {
                    log.info("successful payment webhook request exists");
                }
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
            throw new CustomServerError("error from paystack webhook");
        }
    }

    /**
     * Processes the webhook data when a payment is successful.
     *
     * @param data contains details of a successful payment.
     * @throws CustomServerError if there is an error occurs transforming data to a custom object.
     */
    void onSuccessWebHook(final JsonNode data) {
        paymentDetailService.onSuccessfulPayment(data);
    }

}