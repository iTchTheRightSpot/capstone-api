package dev.webserver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.webserver.category.CategoryResponse;
import dev.webserver.payment.CheckoutPair;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.exception.CustomServerError;
import dev.webserver.payment.TotalProjection;
import dev.webserver.product.DetailProjection;
import dev.webserver.product.PriceCurrencyDto;
import dev.webserver.product.util.CustomMultiPart;
import dev.webserver.product.util.Variant;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static dev.webserver.enumeration.SarreCurrency.NGN;
import static dev.webserver.enumeration.SarreCurrency.USD;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;
import static java.math.RoundingMode.UP;

public class CustomUtil {

    private static final Logger log = LoggerFactory.getLogger(CustomUtil.class);

    /**
     * Converts date to UTC {@link Date}.
     *
     * @param date of type java.util.date
     * @return {@link Date} in utc
     */
    public static Date toUTC(final Date date) {
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
    public static boolean validateContainsCurrencies(
            final PriceCurrencyDto[] dto) {
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
     * {@link DetailProjection}
     * to an array of {@link Variant} objects.
     * <p>
     * This method uses the {@link ObjectMapper} to deserialize
     * the input String into an array of {@link Variant} objects.
     *
     * @param str The String obtained from the getVariants method in
     * {@link DetailProjection}.
     * @param clazz The class that invokes this method.
     * @return An array of {@link Variant} objects if successful,
     * or null if an error occurs during deserialization.
     */
    public static <T> Variant[] toVariantArray(final String str, final T clazz) {
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
            case NGN -> amount.compareTo(ZERO) == 0 ? amount : total.setScale(0, UP);
            case USD -> amount.compareTo(ZERO) == 0 ? amount : total;
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
    public static Cookie cookie (final HttpServletRequest req, final String name) {
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
    public static List<CategoryResponse> createCategoryHierarchy(
            final List<CategoryResponse> list) {
        final Map<Long, CategoryResponse> map = new HashMap<>();

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
    public static BigDecimal calculateTotal(
            final BigDecimal cartItemsTotal, final double tax, final BigDecimal shippingPrice) {
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
     * This method takes a lis of {@link TotalProjection} objects,
     * where each object represents an item in the shopping
     * cart with information about quantity, price, and weight.
     *
     * @param list The list of {@link TotalProjection} items for which
     *             to calculate the total price and weights.
     * @return A {@link CheckoutPair} object containing the total
     * of weight and the total price of the {@code list}.
     */
    public static CheckoutPair cartItemsTotalAndTotalWeight(final List<TotalProjection> list) {
        final double sumOfWeight = list.stream()
                .mapToDouble(TotalProjection::getWeight)
                .sum();

        final BigDecimal total = list.stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQty())))
                .reduce(ZERO, BigDecimal::add)
                .setScale(2, FLOOR);

        return new CheckoutPair(sumOfWeight, total);
    }

    /**
     * Executes a list of tasks asynchronously and returns a {@link CompletableFuture}
     * where all the tasks are complete. Each task is executed independently and
     * concurrently, leveraging the new Virtual Thread.
     *
     * @param schedules The list of tasks to execute asynchronously.
     * @return A {@link CompletableFuture} holding a list of results from all completed
     * tasks.
     * @throws CustomServerError if an error occurs when performing an asynchronous
     * task.
     */
    public static <T> CompletableFuture<List<T>> asynchronousTasks(final List<Supplier<T>> schedules) {
        final List<CompletableFuture<Supplier<T>>> futures = new ArrayList<>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (final Supplier<T> s : schedules) {
                futures.add(CompletableFuture.supplyAsync(() -> s, executor));
            }
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).map(Supplier::get).toList());
    }

    /**
     * Validates if items in {@link MultipartFile} array are all images, else an error is thrown.
     * Note I am returning an array as it is a bit more efficient than arraylist in
     * terms of memory.
     */
    public static BiFunction<MultipartFile[], StringBuilder, CustomMultiPart[]> transformMultipartFile =
            (files, defaultKey) -> Arrays.stream(files)
                    .map(multipartFile -> {
                        final String name = Objects.requireNonNull(multipartFile.getOriginalFilename());

                        final File file = new File(name);

                        try (var stream = new FileOutputStream(file)) {
                            stream.write(multipartFile.getBytes());

                            // validate file is an image
                            final String contentType = Files.probeContentType(file.toPath());
                            if (!contentType.startsWith("image/")) {
                                log.error("File is not an image");
                                throw new CustomServerError("File is not an image");
                            }

                            // create file metadata
                            final Map<String, String> metadata = new HashMap<>();
                            metadata.put("Content-Type", contentType);
                            metadata.put("Title", name);
                            metadata.put("Type", StringUtils.getFilenameExtension(name));

                            // file default key
                            final String key = UUID.randomUUID().toString();
                            if (defaultKey.isEmpty()) {
                                defaultKey.append(key);
                            }

                            // prevents files from being saved to root folder
                            file.deleteOnExit();

                            return new CustomMultiPart(file, metadata, key);
                        } catch (IOException e) {
                            log.error("error either writing multipart to file or getting file type. {}", e.getMessage());
                            throw new CustomServerError("please verify files are images");
                        }
                    }) //
                    .toArray(CustomMultiPart[]::new);
}
