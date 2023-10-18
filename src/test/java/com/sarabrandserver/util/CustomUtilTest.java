package com.sarabrandserver.util;

import com.sarabrandserver.AbstractUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CustomUtilTest extends AbstractUnitTest {

    private record AmountConversion(BigDecimal test, long amount) {}

    @Test
    @DisplayName(value = "validate conversion from usd to cents")
    void cents() {
        AmountConversion[] arr = {
                new AmountConversion(new BigDecimal("0.00"), 0L),
                new AmountConversion(new BigDecimal("0.99"), 99L),
                new AmountConversion(new BigDecimal("1.00"), 100),
                new AmountConversion(new BigDecimal("9.19"), 919L),
                new AmountConversion(new BigDecimal("10.50"), 1050L),
                new AmountConversion(new BigDecimal("15.00"), 1500L),
                new AmountConversion(new BigDecimal("100"), 10000L),
                new AmountConversion(new BigDecimal("150"), 15000L),
                new AmountConversion(new BigDecimal("550."), 55000L),
                new AmountConversion(new BigDecimal("550.20"), 55020L),
                new AmountConversion(new BigDecimal("550.20"), 55020L),
                new AmountConversion(new BigDecimal("1000000.50"), 100000050L),
        };

        for (AmountConversion obj : arr) {
            assertEquals(obj.amount, obj.test.longValue());
        }

    }
}