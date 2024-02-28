package com.sarabrandserver.payment.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.exception.CustomServerError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;

import static java.nio.charset.StandardCharsets.UTF_8;

public class WebHookUtil {

    private static final Logger log = LoggerFactory.getLogger(WebHookUtil.class);

    public static BigDecimal fromNumberToBigDecimal(Number number) {
        return new BigDecimal(number.toString());
    }

    /**
     * Transforms request body into a string
     * */
    public static String httpServletRequestToString(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = req.getReader().readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Validates if request came from paystack
     * <a href="https://paystack.com/docs/payments/webhooks/">...</a>
     * */
    public static WebhookConstruct validateRequestFromPayStack(String secretKey, String body) {
        String hmac = "HmacSHA512";
        try {
            JsonNode node = new ObjectMapper().readValue(body, JsonNode.class);
            Mac sha512_HMAC = Mac.getInstance(hmac);
            sha512_HMAC.init(new SecretKeySpec(secretKey.getBytes(UTF_8), hmac));
            String validate = DatatypeConverter
                    .printHexBinary(sha512_HMAC.doFinal(node.toString().getBytes(UTF_8)));
            return new WebhookConstruct(node, validate);
        } catch (Exception e) {
            log.error("webhook did not come from paystack {}", e.getMessage());
            throw new CustomServerError("webhook did not come from paystack");
        }
    }

}
