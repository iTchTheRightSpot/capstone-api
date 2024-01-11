package com.sarabrandserver.util;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractUnitTest;
import com.sarabrandserver.category.response.CategoryResponse;
import com.sarabrandserver.product.dto.PriceCurrencyDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.sarabrandserver.enumeration.SarreCurrency.NGN;
import static com.sarabrandserver.enumeration.SarreCurrency.USD;
import static org.junit.jupiter.api.Assertions.*;

class CustomUtilTest extends AbstractUnitTest {

    private record AmountConversion(BigDecimal given, long expected) { }

    @Test
    void validate_contains_desired_currencies() {
        PriceCurrencyDTO[] arr = {
                new PriceCurrencyDTO(new BigDecimal(new Faker().commerce().price()), "USD"),
                new PriceCurrencyDTO(new BigDecimal(new Faker().commerce().price()), "NGN"),
        };

        assertTrue(CustomUtil.validateContainsCurrencies(arr));
    }

    @Test
    void error_thrown_from_negative_price() {
        PriceCurrencyDTO[] arr = {
                new PriceCurrencyDTO(new BigDecimal("-1"), "USD"),
                new PriceCurrencyDTO(new BigDecimal(new Faker().commerce().price()), "NGN"),
        };

        assertFalse(CustomUtil.validateContainsCurrencies(arr));
    }

    @Test
    void can_only_be_ngn_and_usd() {
        PriceCurrencyDTO[] arr = {
                new PriceCurrencyDTO(new BigDecimal("9.99"), USD.name()),
                new PriceCurrencyDTO(new BigDecimal("0"), USD.name()),
                new PriceCurrencyDTO(new BigDecimal(new Faker().commerce().price()), NGN.name()),
        };

        assertFalse(CustomUtil.validateContainsCurrencies(arr));
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
            assertEquals(obj.expected(), CustomUtil.convertCurrency(USD, obj.given()));
        } // end of for
    }

    @Test
    @DisplayName("""
    method tests creating a object hierarchy based on data
    received from {@code allCategory} in {@code CategoryResponse} interface
    """)
    void categoryConverter() {
        var actual = CustomUtil.categoryConverter(db());
        assertEquals(res(), actual);
    }

    final List<CategoryResponse> db() {
        return List.of(
                new CategoryResponse(1L, null, "category", true),
                new CategoryResponse(2L, 1L, "clothes", true),
                new CategoryResponse(3L, 2L, "top", true),
                new CategoryResponse(4L, null, "collection", true),
                new CategoryResponse(5L, 4L, "fall 2023", true),
                new CategoryResponse(6L, 4L, "summer 2023", true),
                new CategoryResponse(7L, 5L, "jacket fall 2023", true),
                new CategoryResponse(8L, 3L, "long-sleeve", true)
        );
    }

    final List<CategoryResponse> res() {
        // super parentId
        var category = new CategoryResponse(1L, null, "category", true);

        var clothes = new CategoryResponse(2L, category.id(), "clothes", true);
        category.addToChildren(clothes);

        var top = new CategoryResponse(3L, clothes.id(), "top", true);
        clothes.addToChildren(top);

        top.addToChildren(new CategoryResponse(8L, top.id(), "long-sleeve", true));

        // super parentId
        var collection = new CategoryResponse(4L, null, "collection", true);

        var fall = new CategoryResponse(5L, collection.id(), "fall 2023", true);
        collection.addToChildren(fall);
        fall.addToChildren(new CategoryResponse(7L, fall.id(), "jacket fall 2023", true));

        var summer = new CategoryResponse(6L, collection.id(), "summer 2023", true);
        collection.addToChildren(summer);

        return List.of(category, collection);
    }

}