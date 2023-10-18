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

import java.util.*;
import java.util.function.Function;

import static com.sarabrandserver.enumeration.SarreCurrency.NGN;
import static com.sarabrandserver.enumeration.SarreCurrency.USD;

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
    public Optional<Date> toUTC(Date date) {
        if (date == null) {
            return Optional.empty();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        return Optional.of(calendar.getTime());
    }

    public long toNGN(PriceCurrencyDTO[] dto) {
        PriceCurrencyDTO ngnAmount = Arrays.stream(dto)
                .filter(predicate -> predicate.currency().equals(NGN.name()))
                .findFirst()
                .orElseThrow(() -> new CustomNotFoundException("please enter NGN amount"));
        return convertCurrency(NGN, ngnAmount.price().longValue());
    }

    public long toUSD(PriceCurrencyDTO[] dto) {
        PriceCurrencyDTO usdAmount = Arrays.stream(dto)
                .filter(predicate -> predicate.currency().equals(USD.name()))
                .findFirst()
                .orElseThrow(() -> new CustomNotFoundException("please enter USD amount"));
        return convertCurrency(USD, usdAmount.price().longValue());
    }

    public boolean validateContainsCurrencies(PriceCurrencyDTO[] dto) {
        if (dto.length < 2) {
            return false;
        }

        boolean ngn = Arrays.stream(dto)
                .anyMatch(dto1 -> dto1.currency().equals(NGN.name()));
        boolean usd = Arrays.stream(dto)
                .anyMatch(dto1 -> dto1.currency().equals(USD.name()));

        return ngn && usd;
    }

    public long convertCurrency(SarreCurrency currency, long unitAmount) {
        return switch (currency) {
            // TODO find out how stripe converts NGN
            // 45000 NGN = 450 kobo on stripe
            case NGN -> 0L;

            // 1 cent = 0.01 usd
            // x cent = 10 usd
            // x cent = 10 / 0.01
            case USD -> (long) (unitAmount / 0.01);
        };
    }


    private record VariantHelperMapper(String sku, String inventory, String size) { }
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
            VariantHelperMapper[] mapper = this.objectMapper.readValue(str, VariantHelperMapper[].class);
            Variant[] variant = new Variant[mapper.length];

            for (int i = 0; i < mapper.length; i++) {
                variant[i] = new Variant(mapper[i].sku, mapper[i].inventory, mapper[i].size);
            }

            return variant;
        } catch (JsonProcessingException e) {
            log.error("Error converting from ProductSKUs to Variant. " + clazz);
            return null;
        }
    }

}
