package com.sarabrandserver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.product.dto.PriceCurrencyDTO;
import com.sarabrandserver.product.response.Variant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Function;

import static com.sarabrandserver.enumeration.SarreCurrency.NGN;
import static com.sarabrandserver.enumeration.SarreCurrency.USD;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;

@Service
@RequiredArgsConstructor
public class CustomUtil {

    private static final Logger log = LoggerFactory.getLogger(CustomUtil.class.getName());

    private final ObjectMapper objectMapper;

    /**
     * A custom mapper to reduce boilerplate code
     *
     * @param x is the object received
     * @param function converts from T to S
     * @return S is the return type
     * */
    public <T, S> S customMapper(T x, Function<T, S> function) {
        return function.apply(x);
    }

    /**
     * Converts date to UTC Date
     *
     * @param date of type java.util.date
     * @return Date of type java.util.date
     */
    public Date toUTC(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        return calendar.getTime();
    }

    /**
     * Validates DTO is in the right format
     * */
    public boolean validateContainsCurrencies(PriceCurrencyDTO[] dto) {
        if (dto.length != 2) {
            return false;
        }

        boolean bool = Arrays.stream(dto)
                .anyMatch(p -> {
                    int compare = p.price().compareTo(ZERO);
                    // 1 if compare is greater than BigDecimal.ZERO
                    // -1 if compare is less than BigDecimal.ZERO
                    // 0 if compare is equal to BigDecimal.ZERO
                    return compare < 0;
                });

        if (bool) {
            return false;
        }

        boolean ngn = Arrays.stream(dto)
                .anyMatch(dto1 -> dto1.currency().toUpperCase().equals(NGN.name()));
        boolean usd = Arrays.stream(dto)
                .anyMatch(dto1 -> dto1.currency().toUpperCase().equals(USD.name()));

        return ngn && usd;
    }

    /**
     * Retrieves the price based on the currency.
     *
     * @param arr dto object gotten from the user
     * @param currency gets the price in the array
     * @return BigDecimal
     * */
    public BigDecimal truncateAmount(PriceCurrencyDTO[] arr, SarreCurrency currency) {
        String error = "please enter %s amount".formatted(currency.name());

        PriceCurrencyDTO dto = Arrays
                .stream(arr)
                .filter(predicate -> predicate.currency().equals(currency.name()))
                .findFirst()
                .orElseThrow(() -> new CustomNotFoundException(error));

        return dto.price().setScale(2, FLOOR);
    }

    /**
     * Convert String gotten from DetailPojo getVariant method to
     * a Variant array object
     *
     * @param str is String method getVariants in DetailPojo
     * @param clazz is the class that cause the error
     * @return Variant array
     * */
    public <T> Variant[] toVariantArray(String str, T clazz) {
        try {
            log.info("Variant Mapper before string {}", str);

            VariantHelperMapper[] mapper = this.objectMapper
                    .readValue(str, VariantHelperMapper[].class);

            log.info("Variant Mapper hit");

            Variant[] variant = new Variant[mapper.length];

            for (int i = 0; i < mapper.length; i++) {
                variant[i] = new Variant(mapper[i].sku(), mapper[i].inventory(), mapper[i].size());
            }

            return variant;
        } catch (JsonProcessingException e) {
            log.error("Error converting from ProductSKUs to Variant. " + clazz);
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
    public long convertCurrency(SarreCurrency currency, BigDecimal bigDecimal) {
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

}
