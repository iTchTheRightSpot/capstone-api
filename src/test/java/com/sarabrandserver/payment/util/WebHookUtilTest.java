package com.sarabrandserver.payment.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.AbstractUnitTest;
import com.sarabrandserver.enumeration.SarreCurrency;
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

        assertEquals(new BigDecimal("39450.63"), WebHookUtil
                .fromLowestCurrencyFormToCurrency(kobo, SarreCurrency.NGN)
        );

        assertEquals(new BigDecimal("100"), WebHookUtil
                .fromLowestCurrencyFormToCurrency(cents, SarreCurrency.USD)
        );
    }

}