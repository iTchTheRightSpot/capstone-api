package com.sarabrandserver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.category.response.CategoryResponse;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.product.dto.PriceCurrencyDTO;
import com.sarabrandserver.product.response.Variant;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import static com.sarabrandserver.enumeration.SarreCurrency.NGN;
import static com.sarabrandserver.enumeration.SarreCurrency.USD;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;

public class CustomUtil {

    private static final Logger log = LoggerFactory.getLogger(CustomUtil.class.getName());

    /**
     * Converts date to UTC Date
     *
     * @param date of type java.util.date
     * @return {@code java.util.date} in utc
     */
    public static Date toUTC(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        return calendar.getTime();
    }

    /**
     * Validates DTO is in the right format
     * */
    public static boolean validateContainsCurrencies(PriceCurrencyDTO[] dto) {
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
     * Convert String gotten from DetailPojo getVariant method to
     * a Variant array object
     *
     * @param str is String method getVariants in DetailPojo
     * @param clazz is the class that calls this method
     * @return Variant array
     * */
    public static <T> Variant[] toVariantArray(String str, T clazz) {
        try {
            return new ObjectMapper().readValue(str, Variant[].class);
        } catch (JsonProcessingException e) {
            log.error("error converting from ProductSKUs to Variant. " + clazz);
            return null;
        }
    }

    /**
     * Converts from the amount to the lowest. E.g. converting from USD to cents
     * would be
     * 1 cent = 0.01 usd
     * x cent = 10 usd
     * after cross multiplication,
     * x = (10 / 0.01) or 1000 cents
     */
    public static long convertCurrency(SarreCurrency currency, BigDecimal bigDecimal) {
        return switch (currency) {
            case NGN -> {
                BigDecimal b = bigDecimal.setScale(2, FLOOR);
                int compare = b.compareTo(ZERO);

                if (compare == 0) {
                    yield 0;
                }

                double d = b.doubleValue();
                yield (long) d;

            }
            case USD -> {
                // truncate without rounding and scale 2. meaning leave only 2 nums after .
                BigDecimal b = bigDecimal.setScale(2, FLOOR);
                int compare = b.compareTo(ZERO);

                if (compare == 0) {
                    yield 0;
                }

                // convert to whole number
                // look in unit test class
                double d = b.doubleValue() * 100;
                yield (long) d;
            }
        };
    }

    /**
     * Returns jakarta.servlet.http.cookie based on cookie value passed.
     * Note if response is null, its is either HttpServletRequest contains
     * no cookies or name is not present in request cookies.
     * */
    public static Cookie cookie (HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        return cookies == null
                ? null
                : Arrays.stream(cookies)
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(null);
    };

    /**
     * Creates an object hierarchy based on list of {@code CategoryResponse}
     * */
    public static List<CategoryResponse> createCategoryHierarchy(List<CategoryResponse> list) {
        Map<Long, CategoryResponse> map = new HashMap<>();

        // hierarchy is built by inject root
        map.put(-1L, new CategoryResponse("root"));

        // add all to map
        for (CategoryResponse cat : list) {
            map.put(cat.id(), cat);
        }

        for (CategoryResponse entry : list) {
            if (entry.parent() == null) {
                // create a new leaf from root as parentId category seen
                map.get(-1L).addToChildren(entry);
            } else {
                // add child to parentId
                var parent = map.get(entry.parent());
                var child = map.get(entry.id());
                parent.addToChildren(child);
            }
        }

        return map.get(-1L).children(); // return children of root
    }

}
