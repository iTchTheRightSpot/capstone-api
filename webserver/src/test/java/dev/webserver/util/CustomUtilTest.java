package dev.webserver.util;

import com.github.javafaker.Faker;
import dev.webserver.AbstractUnitTest;
import dev.webserver.TestData;
import dev.webserver.category.CategoryResponse;
import dev.webserver.payment.CartTotalDbMapper;
import dev.webserver.payment.CheckoutPair;
import dev.webserver.product.PriceCurrencyDto;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static dev.webserver.enumeration.CapstoneCurrency.NGN;
import static dev.webserver.enumeration.CapstoneCurrency.USD;
import static java.math.RoundingMode.FLOOR;
import static org.junit.jupiter.api.Assertions.*;

final class CustomUtilTest extends AbstractUnitTest {

    private record AmountConversion(BigDecimal given, BigDecimal expected) { }

    @Test
    void shouldSuccessfullyCreateTransformMultipartFilesToFile() throws IOException {
        // given
        final var mockFiles = TestData.files();

        // when
        final var objs = CustomUtil.transformMultipartFile.apply(mockFiles, new StringBuilder());

        // then
        for (final var obj : objs) {
            assertTrue(Files.exists(obj.file().toPath()));
            final var body = RequestBody.fromFile(obj.file());
            assertEquals(Files.probeContentType(obj.file().toPath()), body.contentType());
            assertFalse(obj.key().isBlank());
            assertFalse(obj.metadata().isEmpty());
        }
    }

    @Test
    public void testCartItemsTotalAndTotalWeightNGN() {
        // given
        final List<CartTotalDbMapper> list = List.of(
                new CartTotalDbMapper(1, new BigDecimal("1800"), 2.5),
                new CartTotalDbMapper(5, new BigDecimal("20750"), 3.5),
                new CartTotalDbMapper(2, new BigDecimal("39065"), 5.0)
        );

        // when
        final CheckoutPair test = CustomUtil.cartItemsTotalAndTotalWeight(list);

        // when
        assertEquals(test.sumOfWeight(), 11);
        assertEquals(test.total(), new BigDecimal("183680.00"));
    }

    @Test
    public void testCartItemsTotalAndTotalWeightUSD() {
        // given
        final List<CartTotalDbMapper> list = List.of(
                new CartTotalDbMapper(3, new BigDecimal("110.00"), 10.3),
                new CartTotalDbMapper(1, new BigDecimal("120.00"), 1.4),
                new CartTotalDbMapper(5, new BigDecimal("30.39"), 6.7)
        );

        // when
        final CheckoutPair test = CustomUtil.cartItemsTotalAndTotalWeight(list);

        // when
        assertEquals(test.sumOfWeight(), 18.4);
        assertEquals(test.total(), new BigDecimal("601.95"));
    }

    @Test
    void calculateTotalInNGN() {
        // when
        final BigDecimal res = CustomUtil
                .calculateTotal(new BigDecimal("1200"), new BigDecimal("0.0725"), new BigDecimal("500"))
                .setScale(2, FLOOR);

        // then
        assertEquals(new BigDecimal("1787.00"), res);
    }

    @Test
    void calculateTotalInUSD() {
        // when
        final BigDecimal res = CustomUtil
                .calculateTotal(new BigDecimal("75.00"), new BigDecimal("0.05"), new BigDecimal("10.48"))
                .setScale(2, FLOOR);

        // then
        assertEquals(new BigDecimal("89.23"), res);
    }

    @Test
    void validateContainsDesiredCurrencies() {
        final PriceCurrencyDto[] arr = {
                new PriceCurrencyDto(new BigDecimal(new Faker().commerce().price()), "USD"),
                new PriceCurrencyDto(new BigDecimal(new Faker().commerce().price()), "NGN"),
        };

        assertTrue(CustomUtil.validateContainsCurrencies(arr));
    }

    @Test
    void errorThrownFromNegativePrice() {
        final PriceCurrencyDto[] arr = {
                new PriceCurrencyDto(new BigDecimal("-1"), "USD"),
                new PriceCurrencyDto(new BigDecimal(new Faker().commerce().price()), "NGN"),
        };

        assertFalse(CustomUtil.validateContainsCurrencies(arr));
    }

    @Test
    void canOnlyBeNgnAndUsd() {
        final PriceCurrencyDto[] arr = {
                new PriceCurrencyDto(new BigDecimal("9.99"), USD.name()),
                new PriceCurrencyDto(new BigDecimal("0"), USD.name()),
                new PriceCurrencyDto(new BigDecimal(new Faker().commerce().price()), NGN.name()),
        };

        assertFalse(CustomUtil.validateContainsCurrencies(arr));
    }

    @Test
    void fromNairaToKobo() {
        final AmountConversion[] arr = {
                new AmountConversion(new BigDecimal("0"), new BigDecimal("0")),
                new AmountConversion(new BigDecimal("1"), new BigDecimal("1")),
                new AmountConversion(new BigDecimal("20.00"), new BigDecimal("7")),
        };

        for (final AmountConversion obj : arr) {
            assertEquals(obj.expected(), CustomUtil.convertCurrency("0.34", NGN, obj.given()));
        }
    }

    @Test
    void fromUsdToCent() {
        final AmountConversion[] arr = {
                new AmountConversion(new BigDecimal("0"), new BigDecimal("0")),
                new AmountConversion(new BigDecimal("1"), new BigDecimal("100.00")),
                new AmountConversion(new BigDecimal("20.00"), new BigDecimal("2000.00")),
        };

        for (final AmountConversion obj : arr) {
            assertEquals(obj.expected(), CustomUtil.convertCurrency("100", USD, obj.given()));
        }
    }

    @Test
    void shouldCreateHierarchyForCategory() {
        assertEquals(res(), CustomUtil.createCategoryHierarchy(db));
    }

    private static final List<CategoryResponse> db = List.of(
            new CategoryResponse(1, null, "category", true, new ArrayList<>()),
            new CategoryResponse(2, 1L, "clothes", true, new ArrayList<>()),
            new CategoryResponse(3, 2L, "top", true, new ArrayList<>()),
            new CategoryResponse(4, null, "collection", true, new ArrayList<>()),
            new CategoryResponse(5, 4L, "fall 2023", true, new ArrayList<>()),
            new CategoryResponse(6, 4L, "summer 2023", true, new ArrayList<>()),
            new CategoryResponse(7, 5L, "jacket fall 2023", true, new ArrayList<>()),
            new CategoryResponse(8, 3L, "long-sleeve", true, new ArrayList<>())
    );

    private static List<CategoryResponse> res() {
        // super parentId
        final var category = CategoryResponse.builder()
                .categoryId(1).parentId(null).name("category").visible(true).children(new ArrayList<>()).build();

        final var clothes = CategoryResponse.builder().categoryId(2).parentId(category.categoryId()).name("clothes").visible(true).children(new ArrayList<>()).build();
        category.addToChildren(clothes);

        final var top = new CategoryResponse(3L, clothes.categoryId(), "top", true, new ArrayList<>());
        clothes.addToChildren(top);

        top.addToChildren(new CategoryResponse(8L, top.categoryId(), "long-sleeve", true, new ArrayList<>()));

        // super parentId
        final var collection = new CategoryResponse(4L, null, "collection", true, new ArrayList<>());

        final var fall = new CategoryResponse(5L, collection.categoryId(), "fall 2023", true, new ArrayList<>());
        collection.addToChildren(fall);
        fall.addToChildren(new CategoryResponse(7L, fall.categoryId(), "jacket fall 2023", true, new ArrayList<>()));

        final var summer = new CategoryResponse(6L, collection.categoryId(), "summer 2023", true, new ArrayList<>());
        collection.addToChildren(summer);

        return List.of(category, collection);
    }

}
