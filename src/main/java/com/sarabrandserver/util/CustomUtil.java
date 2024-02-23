package com.sarabrandserver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.category.response.CategoryResponse;
import com.sarabrandserver.checkout.CheckoutPair;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.CustomServerError;
import com.sarabrandserver.payment.projection.TotalPojo;
import com.sarabrandserver.product.dto.PriceCurrencyDto;
import com.sarabrandserver.product.response.Variant;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.sarabrandserver.enumeration.SarreCurrency.NGN;
import static com.sarabrandserver.enumeration.SarreCurrency.USD;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;

public class CustomUtil {

    private static final Logger log = LoggerFactory.getLogger(CustomUtil.class);

    /**
     * Converts date to UTC Date
     *
     * @param date of type java.util.date
     * @return {@link java.util.Date} in utc
     */
    public static Date toUTC(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        return calendar.getTime();
    }

    /**
     * Validates that the provided array of {@link PriceCurrencyDto} objects is
     * in the correct format.
     * <p>
     * This method checks whether the array contains exactly two elements, and
     * verifies that each {@link PriceCurrencyDto} object has a non-negative
     * price value. Additionally, it ensures that the array contains both only
     * {@link SarreCurrency}.
     *
     * @param dto An array of {@link PriceCurrencyDto} objects to be validated.
     * @return true if the array is in the correct format and contains only
     * {@link SarreCurrency}.
     */
    public static boolean validateContainsCurrencies(PriceCurrencyDto[] dto) {
        if (dto.length != 2) {
            return false;
        }

        // 1 if compare is greater than BigDecimal.ZERO
        // -1 if compare is less than BigDecimal.ZERO
        // 0 if compare is equal to BigDecimal.ZERO
        boolean bool = Arrays.stream(dto)
                .anyMatch(p -> p.price().compareTo(ZERO) < 0);

        if (bool) {
            return false;
        }

        boolean ngn = Arrays.stream(dto)
                .anyMatch(d -> d.currency().toUpperCase().equals(NGN.name()));
        boolean usd = Arrays.stream(dto)
                .anyMatch(d -> d.currency().toUpperCase().equals(USD.name()));

        return ngn && usd;
    }

    /**
     * Converts a String obtained from the getVariants method of
     * {@link com.sarabrandserver.product.projection.DetailPojo}
     * to an array of {@link Variant} objects.
     * <p>
     * This method uses the {@link ObjectMapper} to deserialize
     * the input String into an array of {@link Variant} objects.
     *
     * @param str The String obtained from the getVariants method in
     * {@link com.sarabrandserver.product.projection.DetailPojo}.
     * @param clazz The class that invokes this method.
     * @return An array of {@link Variant} objects if successful,
     * or null if an error occurs during deserialization.
     */
    public static <T> Variant[] toVariantArray(String str, T clazz) {
        try {
            return new ObjectMapper().readValue(str, Variant[].class);
        } catch (JsonProcessingException e) {
            log.error("error converting from ProductSKUs to Variant. " + clazz);
            return null;
        }
    }

    /**
     * Converts the given amount to the lowest denomination of the
     * specified currency. For example, converting from USD to cents
     * would result in 1 dollar being equal to 100 cents, so 1 cent
     * is equivalent to 0.01 dollars.
     *
     * @param currencyConversion The conversion rate to the lowest
     *                           currency denomination.
     * @param currency The currency to convert to, represented by
     * {@link SarreCurrency}.
     * @param amount The amount to convert to the lowest currency denomination.
     * @return The amount converted to the lowest currency denomination,
     * represented as a {@link BigDecimal}.
     */
    public static BigDecimal convertCurrency(
            final String currencyConversion,
            final SarreCurrency currency,
            final BigDecimal amount
    ) {
        final BigDecimal total = amount
                .multiply(new BigDecimal(currencyConversion))
                .setScale(2, FLOOR);
        return switch (currency) {
            case NGN, USD -> amount.compareTo(ZERO) == 0 ? amount : total;
        };
    }

    /**
     * Retrieves a {@link Cookie} based on the specified cookie name from
     * the {@link HttpServletRequest}.
     * <p>
     * This method searches for a cookie with the provided name in the
     * {@link HttpServletRequest}. If the request contains no cookies or if
     * the specified cookie name is not found, null is returned.
     *
     * @param req The {@link HttpServletRequest} object from which to
     *            retrieve cookies.
     * @param name The name of the cookie to retrieve.
     * @return The {@link Cookie} object with the specified name, or null
     * if not found.
     */
    public static Cookie cookie (HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        return cookies == null
                ? null
                : Arrays.stream(cookies)
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Constructs an object hierarchy based on a list of {@link CategoryResponse} objects.
     * <p>
     * This method takes a list of {@link CategoryResponse} objects as input, where each object
     * represents a category with attributes such as categoryId, parentId, categoryName, visibility,
     * and a list of child categories.
     * <p>
     * The method constructs a hierarchical structure of categories, with each category linked to its
     * parent category based on the parentId attribute. The root category is assigned a parentId of -1L.
     *
     * @param list A list of {@link CategoryResponse} objects representing categories to be organized
     *             into a hierarchy.
     * @return A list of {@link CategoryResponse} objects representing the root categories in the hierarchy.
     */
    public static List<CategoryResponse> createCategoryHierarchy(List<CategoryResponse> list) {
        Map<Long, CategoryResponse> map = new HashMap<>();

        // hierarchy is built by inject root
        map.put(-1L, new CategoryResponse("root"));

        // add all to map
        for (CategoryResponse cat : list) {
            map.put(cat.categoryId(), cat);
        }

        for (CategoryResponse entry : list) {
            if (entry.parentId() == null) {
                // create a new leaf from root as parentId category seen
                map.get(-1L).addToChildren(entry);
            } else {
                // add child to parentId
                var parent = map.get(entry.parentId());
                var child = map.get(entry.categoryId());
                parent.addToChildren(child);
            }
        }

        return map.get(-1L).children(); // return children of root
    }

    /**
     * Calculates the total cost of the shopping cart including tax rate and shipping.
     * <p>
     * This method takes the total cost of cart items, the tax rate, and the shipping price
     * as input parameters and computes the total cost of the shopping cart including tax and shipping.
     *
     * @param cartItemsTotal The total cost of the items in the shopping cart before tax and shipping.
     * @param tax            The tax rate applied to the total cost of cart items (in percentage).
     * @param shippingPrice  The price for shipping the items in the shopping cart.
     * @return The total cost of the shopping cart including tax and shipping as a BigDecimal value.
     */
    public static BigDecimal calculateTotal(BigDecimal cartItemsTotal, double tax, BigDecimal shippingPrice) {
        var newTax = cartItemsTotal.multiply(BigDecimal.valueOf(tax));
        return cartItemsTotal
                .add(newTax)
                .add(shippingPrice);
    }

    /**
     * Calculates the total weight and cost for each item in a
     * users shopping cart.
     * <p>
     * The total cost for each item is calculated by using the
     * formula: total = weight + (price * quantity).
     * <p>
     * This method takes a lis of {@link TotalPojo} objects,
     * where each object represents an item in the shopping
     * cart with information about quantity, price, and weight.
     *
     * @param list The list of {@link TotalPojo} items for which
     *             to calculate the total price and weights.
     * @return A {@link CheckoutPair} object containing the total
     * of weight and the total price of the {@code list}.
     */
    public static CheckoutPair cartItemsTotalAndTotalWeight(List<TotalPojo> list) {
        double sumOfWeight = list.stream()
                .mapToDouble(TotalPojo::getWeight)
                .sum();

        BigDecimal total = list.stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQty())))
                .reduce(ZERO, BigDecimal::add)
                .setScale(2, FLOOR);

        return new CheckoutPair(sumOfWeight, total);
    }

    /**
     * Executes a list of tasks asynchronously and returns a {@link CompletableFuture}
     * where all the tasks are complete. Each task is executed independently and
     * concurrently, leveraging the Virtual Thread.
     *
     * @param schedules The list of tasks to execute asynchronously.
     * @param <T>       The type of the tasks to be executed.
     * @return A {@link CompletableFuture} holding a list of results from all completed tasks.
     */
    public static <T> CompletableFuture<List<T>> asynchronousTasks(List<T> schedules) {
        List<CompletableFuture<T>> futures = new ArrayList<>();
        try (final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (T s : schedules) {
                futures.add(CompletableFuture
                        .supplyAsync(() -> s, executor)
                        .exceptionally(e -> {
                            log.error(e.getMessage());
                            throw new CustomServerError(e.getMessage());
                        })
                );
            }
        }
        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());
    }

}
