package com.sarabrandserver.util;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractUnitTest;
import com.sarabrandserver.category.response.CategoryResponse;
import com.sarabrandserver.product.dto.PriceCurrencyDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.sarabrandserver.enumeration.SarreCurrency.NGN;
import static com.sarabrandserver.enumeration.SarreCurrency.USD;
import static java.math.RoundingMode.FLOOR;
import static org.junit.jupiter.api.Assertions.*;

class CustomUtilTest extends AbstractUnitTest {

    private record AmountConversion(BigDecimal given, BigDecimal expected) { }

    @Test
    void calculateTotalInNGN() {
        // when
        BigDecimal res = CustomUtil
                .calculateTotal(new BigDecimal("1200"), 0.0725, new BigDecimal("500"))
                .setScale(2, FLOOR);

        // then
        assertEquals(new BigDecimal("1787.00"), res);
    }

    @Test
    void calculateTotalInUSD() {
        // when
        BigDecimal res = CustomUtil
                .calculateTotal(new BigDecimal("75.00"), 0.05, new BigDecimal("10.48"))
                .setScale(2, FLOOR);

        // then
        assertEquals(new BigDecimal("89.23"), res);
    }

    @Test
    void validate_contains_desired_currencies() {
        PriceCurrencyDto[] arr = {
                new PriceCurrencyDto(new BigDecimal(new Faker().commerce().price()), "USD"),
                new PriceCurrencyDto(new BigDecimal(new Faker().commerce().price()), "NGN"),
        };

        assertTrue(CustomUtil.validateContainsCurrencies(arr));
    }

    @Test
    void error_thrown_from_negative_price() {
        PriceCurrencyDto[] arr = {
                new PriceCurrencyDto(new BigDecimal("-1"), "USD"),
                new PriceCurrencyDto(new BigDecimal(new Faker().commerce().price()), "NGN"),
        };

        assertFalse(CustomUtil.validateContainsCurrencies(arr));
    }

    @Test
    void can_only_be_ngn_and_usd() {
        PriceCurrencyDto[] arr = {
                new PriceCurrencyDto(new BigDecimal("9.99"), USD.name()),
                new PriceCurrencyDto(new BigDecimal("0"), USD.name()),
                new PriceCurrencyDto(new BigDecimal(new Faker().commerce().price()), NGN.name()),
        };

        assertFalse(CustomUtil.validateContainsCurrencies(arr));
    }

    @Test
    void fromNairaToKobo() {
        AmountConversion[] arr = {
                new AmountConversion(new BigDecimal("0"), new BigDecimal("0")),
                new AmountConversion(new BigDecimal("1"), new BigDecimal("0.34")),
                new AmountConversion(new BigDecimal("20.00"), new BigDecimal("6.80")),
        };

        for (AmountConversion obj : arr) {
            assertEquals(obj.expected(), CustomUtil
                    .convertCurrency("0.34", NGN, obj.given())
            );
        }
    }

    @Test
    void fromUsdToCent() {
        AmountConversion[] arr = {
                new AmountConversion(new BigDecimal("0"), new BigDecimal("0")),
                new AmountConversion(new BigDecimal("1"), new BigDecimal("100.00")),
                new AmountConversion(new BigDecimal("20.00"), new BigDecimal("2000.00")),
        };

        for (AmountConversion obj : arr) {
            assertEquals(obj.expected(), CustomUtil
                    .convertCurrency("100", USD, obj.given())
            );
        }
    }

    @Test
    @DisplayName("""
    method tests creating a object hierarchy based on data
    received from {@code allCategory} in {@code CategoryResponse} interface
    """)
    void categoryConverter() {
        var actual = CustomUtil.createCategoryHierarchy(db());
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

        var clothes = new CategoryResponse(2L, category.categoryId(), "clothes", true);
        category.addToChildren(clothes);

        var top = new CategoryResponse(3L, clothes.categoryId(), "top", true);
        clothes.addToChildren(top);

        top.addToChildren(new CategoryResponse(8L, top.categoryId(), "long-sleeve", true));

        // super parentId
        var collection = new CategoryResponse(4L, null, "collection", true);

        var fall = new CategoryResponse(5L, collection.categoryId(), "fall 2023", true);
        collection.addToChildren(fall);
        fall.addToChildren(new CategoryResponse(7L, fall.categoryId(), "jacket fall 2023", true));

        var summer = new CategoryResponse(6L, collection.categoryId(), "summer 2023", true);
        collection.addToChildren(summer);

        return List.of(category, collection);
    }

}