package dev.webserver.payment.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.webserver.AbstractUnitTest;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.payment.WebHookUtil;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebHookUtilTest extends AbstractUnitTest {

    @Test
    void fromNumberToBigDecimal() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode node = mapper
                .readValue("{\"amount\": 50000}", JsonNode.class);

        BigDecimal intAmount = WebHookUtil
                .fromNumberToBigDecimal(mapper.treeToValue(node.get("amount"), Number.class));

        assertEquals(new BigDecimal("50000"), intAmount);

        JsonNode node1 = mapper
                .readValue("{\"amount\": 50000.52}", JsonNode.class);

        BigDecimal doubleAmount = WebHookUtil
                .fromNumberToBigDecimal(mapper.treeToValue(node1.get("amount"), Number.class));
        assertEquals(new BigDecimal("50000.52"), doubleAmount);
    }

    @Test
    void fromLowestCurrencyFormToCurrency() {
        BigDecimal kobo = new BigDecimal("5000");
        BigDecimal cents = new BigDecimal("10000");

        assertEquals(new BigDecimal("39650.00"), WebHookUtil
                .fromLowestCurrencyFormToCurrency(kobo, SarreCurrency.NGN)
        );

        assertEquals(new BigDecimal("100.00"), WebHookUtil
                .fromLowestCurrencyFormToCurrency(cents, SarreCurrency.USD)
        );
    }

}