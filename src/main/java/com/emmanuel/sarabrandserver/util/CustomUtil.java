package com.emmanuel.sarabrandserver.util;

import com.emmanuel.sarabrandserver.product.util.Variant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Logger;

@Service
public class CustomUtil {
    private static final Logger log = Logger.getLogger(CustomUtil.class.getName());

    private final ObjectMapper objectMapper;

    public CustomUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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
            return this.objectMapper.readValue(str, Variant[].class);
        } catch (JsonProcessingException e) {
            log.warning("Error converting from ProductSKUs to Variant. " + clazz);
            return null;
        }
    }

}
