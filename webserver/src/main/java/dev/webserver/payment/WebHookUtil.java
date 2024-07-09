package dev.webserver.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.webserver.enumeration.SarreCurrency;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static java.math.RoundingMode.FLOOR;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class WebHookUtil {

    private static final Logger log = LoggerFactory.getLogger(WebHookUtil.class);

    public static BigDecimal fromNumberToBigDecimal(Number number) {
        return new BigDecimal(number.toString());
    }

    public static BigDecimal fromLowestCurrencyFormToCurrency(BigDecimal amount, SarreCurrency currency) {
        return switch (currency) {
            // 1 kobo = 7.93 naira as per https://www.coinbase.com/en-gb/converter/kobo/ngn
            case NGN -> amount.multiply(new BigDecimal("7.93")).setScale(2, FLOOR);
            case USD -> amount.divide(new BigDecimal("100"), FLOOR).setScale(2, FLOOR);
        };
    }

    /**
     * Transforms {@link HttpServletRequest} into a string.
     * */
    public static String httpServletRequestToString(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = req.getReader().readLine()) != null) {
            sb.append(line);
        }
        log.info("successfully transformed HttpServletRequest to a String");
        return sb.toString();
    }

    /**
     * Validates if request came from paystack.
     * @see <a href="https://paystack.com/docs/payments/webhooks/">documentation</a>
     * */
    public static WebhookConstruct validateRequestFromPayStack(
            String secretKey, String body
    ) throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeyException {
        String hmac = "HmacSHA512";
        JsonNode node = new ObjectMapper().readValue(body, JsonNode.class);
        Mac sha512_HMAC = Mac.getInstance(hmac);
        sha512_HMAC.init(new SecretKeySpec(secretKey.getBytes(UTF_8), hmac));
        String validate = DatatypeConverter
                .printHexBinary(sha512_HMAC.doFinal(node.toString().getBytes(UTF_8)));
        log.info("successfully constructed WebhookConstruct from {}", WebHookUtil.class.getName());
        return new WebhookConstruct(node, validate);
    }

}
