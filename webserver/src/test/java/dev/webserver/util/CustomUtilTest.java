package dev.webserver.util;

import com.github.javafaker.Faker;
import dev.webserver.AbstractUnitTest;
import dev.webserver.category.CategoryResponse;
import dev.webserver.checkout.CheckoutPair;
import dev.webserver.data.TestData;
import dev.webserver.payment.TotalProjection;
import dev.webserver.product.PriceCurrencyDto;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;

import static dev.webserver.enumeration.SarreCurrency.NGN;
import static dev.webserver.enumeration.SarreCurrency.USD;
import static java.math.RoundingMode.FLOOR;
import static org.junit.jupiter.api.Assertions.*;

class CustomUtilTest extends AbstractUnitTest {

    private record AmountConversion(BigDecimal given, BigDecimal expected) { }

    private record HelperObj(int qty, BigDecimal price, double weight) implements TotalProjection {
        @Override
        public Integer getQty() {
            return HelperObj.this.qty;
        }

        @Override
        public BigDecimal getPrice() {
            return HelperObj.this.price;
        }

        @Override
        public Double getWeight() {
            return HelperObj.this.weight;
        }
    }

    @Test
    void shouldSuccessfullyCreateTransformMultipartFilesToFile() throws IOException {
        // given
        var mockFiles = TestData.files();

        // when
        var objs = CustomUtil.transformMultipartFile.apply(mockFiles, new StringBuilder());

        // then
        for (var obj : objs) {
            assertTrue(Files.exists(obj.file().toPath()));
            var body = RequestBody.fromFile(obj.file());
            assertEquals(Files.probeContentType(obj.file().toPath()), body.contentType());
            assertFalse(obj.key().isBlank());
            assertFalse(obj.metadata().isEmpty());
        }
    }

    @Test
    public void testCartItemsTotalAndTotalWeightNGN() {
        // given
        final List<TotalProjection> list = List.of(
                new HelperObj(1, new BigDecimal("1800"), 2.5),
                new HelperObj(5, new BigDecimal("20750"), 3.5),
                new HelperObj(2, new BigDecimal("39065"), 5)
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
        final List<TotalProjection> list = List.of(
                new HelperObj(3, new BigDecimal("110.00"), 10.3),
                new HelperObj(1, new BigDecimal("120.00"), 1.4),
                new HelperObj(5, new BigDecimal("30.39"), 6.7)
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
            assertEquals(obj.expected(), CustomUtil
                    .convertCurrency("0.34", NGN, obj.given()));
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
    void shouldCreateHierarchyForCategory() {
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
