package com.emmanuel.sarabrandserver.util;

import com.emmanuel.sarabrandserver.product.util.Variant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Function;


@Service
public class CustomUtil {
    private static final Logger log = LoggerFactory.getLogger(CustomUtil.class.getName());

    private final ObjectMapper objectMapper;

    public CustomUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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
            MapOut[] mapOuts = this.objectMapper.readValue(str, MapOut[].class);
            Variant[] variant = new Variant[mapOuts.length];

            for (int i = 0; i < mapOuts.length; i++) {
                variant[i] = new Variant(mapOuts[i].sku, mapOuts[i].inventory, mapOuts[i].size);
            }

            return variant;
        } catch (JsonProcessingException e) {
            log.error("Error converting from ProductSKUs to Variant. " + clazz);
            return null;
        }
    }

    @Getter
    private static class MapOut {
        private String sku;
        private String inventory;
        private String size;

        public MapOut() { }

        public MapOut(String sku, String inventory, String size) {
            this.sku = sku;
            this.inventory = inventory;
            this.size = size;
        }
    }

}
