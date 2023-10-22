package com.sarabrandserver.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractUnitTest;
import com.sarabrandserver.product.dto.PriceCurrencyDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;

import static com.sarabrandserver.enumeration.SarreCurrency.USD;
import static org.junit.jupiter.api.Assertions.*;

class CustomUtilTest extends AbstractUnitTest {

    @Mock ObjectMapper mapper;

    private CustomUtil customUtil;

    @BeforeEach
    void setUp() {
        this.customUtil = new CustomUtil(mapper);
    }

    private record AmountConversion(BigDecimal given, long expected) { }

    @Test
    void validate_contains_desired_currencies() {
        PriceCurrencyDTO[] arr = {
                new PriceCurrencyDTO(new BigDecimal(new Faker().commerce().price()), "USD"),
                new PriceCurrencyDTO(new BigDecimal(new Faker().commerce().price()), "NGN"),
        };

        assertTrue(this.customUtil.validateContainsCurrencies(arr));
    }

    @Test
    void error_thrown_from_negative_price() {
        PriceCurrencyDTO[] arr = {
                new PriceCurrencyDTO(new BigDecimal("-1"), "USD"),
                new PriceCurrencyDTO(new BigDecimal(new Faker().commerce().price()), "NGN"),
        };

        assertFalse(this.customUtil.validateContainsCurrencies(arr));
    }

    @Test
    void can_only_be_ngn_and_usd() {
        PriceCurrencyDTO[] arr = {
                new PriceCurrencyDTO(new BigDecimal("9.99"), "USD"),
                new PriceCurrencyDTO(new BigDecimal("0"), "USD"),
                new PriceCurrencyDTO(new BigDecimal(new Faker().commerce().price()), "NGN"),
        };

        assertFalse(this.customUtil.validateContainsCurrencies(arr));
    }

    @Test
    @DisplayName(value = "validate conversion from usd to cents")
    void cents() {
        AmountConversion[] arr = {
                new AmountConversion(new BigDecimal("0"), 0L),
                new AmountConversion(new BigDecimal("0.00"), 0L),
                new AmountConversion(new BigDecimal("0.010"), 1L),
                new AmountConversion(new BigDecimal("0.013"), 1L),
                new AmountConversion(new BigDecimal("0.015"), 1L),
                new AmountConversion(new BigDecimal("0.0158"), 1L),
                new AmountConversion(new BigDecimal("0.10"), 10L),
                new AmountConversion(new BigDecimal("0.13"), 13L),
                new AmountConversion(new BigDecimal("0.15"), 15L),
                new AmountConversion(new BigDecimal("2"), 200L),
                new AmountConversion(new BigDecimal("2.00"), 200L),
                new AmountConversion(new BigDecimal("2.15"), 215L),
                new AmountConversion(new BigDecimal("2.154"), 215L),
                new AmountConversion(new BigDecimal("2.158"), 215L),
                new AmountConversion(new BigDecimal("10"), 1000L),
                new AmountConversion(new BigDecimal("10.50"), 1050L),
                new AmountConversion(new BigDecimal("0.99"), 99L),
                new AmountConversion(new BigDecimal("1.00"), 100),
                new AmountConversion(new BigDecimal("9.19"), 919L),
                new AmountConversion(new BigDecimal("10.50"), 1050L),
                new AmountConversion(new BigDecimal("15.00"), 1500L),
                new AmountConversion(new BigDecimal("100"), 10000L),
                new AmountConversion(new BigDecimal("150"), 15000L),
                new AmountConversion(new BigDecimal("550."), 55000L),
                new AmountConversion(new BigDecimal("550.20"), 55020L),
                new AmountConversion(new BigDecimal("1000000.50"), 100000050L),
        };

        for (AmountConversion obj : arr) {
            assertEquals(obj.expected(), this.customUtil.convertCurrency(USD, obj.given()));
        } // end of for
    }

}