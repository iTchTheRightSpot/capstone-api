package dev.webserver.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import dev.webserver.exception.CustomServerError;
import dev.webserver.payment.util.WebHookUtil;
import dev.webserver.payment.util.WebhookConstruct;
import dev.webserver.thirdparty.ThirdPartyPaymentService;
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
            String body = WebHookUtil.httpServletRequestToString(req);

            WebhookConstruct pair = WebHookUtil
                    .validateRequestFromPayStack(thirdPartyService.payStackCredentials().secretKey(), body);

            if (!pair.validate().toLowerCase().equals(req.getHeader("x-paystack-signature"))) {
                log.error("invalid request from paystack");
                throw new CustomServerError("invalid webhook from paystack");
            }

            if (pair.node().get("event").textValue().equals("charge.success")
                    && pair.node().get("data").get("status").textValue().equals("success")
            ) {
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
     * @param data contains details of a successful payment.
     * @throws CustomServerError if there is an error occurs transforming data to a custom object.
     */
    void onSuccessWebHook(JsonNode data) {
        paymentDetailService.onSuccessfulPayment(data);
    }

}