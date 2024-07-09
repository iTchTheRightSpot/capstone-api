package dev.webserver.payment;

import com.fasterxml.jackson.databind.JsonNode;
import dev.webserver.exception.CustomServerError;
import dev.webserver.external.log.ILogEventPublisher;
import dev.webserver.payment.util.WebHookUtil;
import dev.webserver.payment.util.WebhookConstruct;
import dev.webserver.external.ThirdPartyPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final ThirdPartyPaymentService thirdPartyService;
    private final PaymentDetailService paymentDetailService;
    private final ILogEventPublisher publisher;

    /**
     * Processes a payment received via webhook from Paystack.
     * Reference <a href="https://paystack.com/docs/payments/webhooks/">documentation</a>
     *
     * @param req the {@link HttpServletRequest} containing the webhook data.
     * @throws CustomServerError if there is an error parsing the request or an invalid request
     * is received from Paystack.
     */
    public void webhook(HttpServletRequest req) {
        try {
            log.info("webhook received");
            final String body = WebHookUtil.httpServletRequestToString(req);

            final WebhookConstruct pair = WebHookUtil
                    .validateRequestFromPayStack(thirdPartyService.payStackCredentials().secretKey(), body);

            if (!pair.validate().toLowerCase().equals(req.getHeader("x-paystack-signature"))) {
                log.error("invalid request from paystack");
                throw new CustomServerError("invalid webhook from paystack");
            }

            final JsonNode data = pair.node().get("data");
            if (pair.node().get("event").textValue().equals("charge.success") && data.get("status").textValue().equals("success")) {
                final String reference = data.get("reference").textValue();
                final JsonNode metadata = data.get("metadata");
                final String email = metadata.get("email").asText();

                if (!paymentDetailService.paymentDetailExists(email, reference)) {
                    onSuccessWebHook(data);
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
            throw new CustomServerError(e.getMessage());
        }
    }

    /**
     * Processes the webhook data when a payment is successful.
     *
     * @param data contains details of a successful payment.
     * @throws CustomServerError if there is an error occurs transforming data to a custom object.
     */
    void onSuccessWebHook(JsonNode data) {
        paymentDetailService.onSuccessfulPayment(data);
    }

}