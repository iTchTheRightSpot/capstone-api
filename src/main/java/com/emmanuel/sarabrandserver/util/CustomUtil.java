package com.emmanuel.sarabrandserver.util;

import com.emmanuel.sarabrandserver.exception.InvalidFormat;
import com.emmanuel.sarabrandserver.product.util.SizeInventoryDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

@Service
public class CustomUtil {

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
     * Transform json format received from UI into desired format
     * e.g. from (String)"{"qty":10,"size":"small"}" to SizeInventoryDTO(qty = 10, size = "small")
     *
     * @param arr is a String arr of SizeInventoryDTO
     * @return SizeInventoryDTO[]
     */
    public SizeInventoryDTO[] converter(String[] arr) {
        SizeInventoryDTO[] res = new SizeInventoryDTO[arr.length];
        for (int i = 0; i < arr.length; i++) {
            String str = arr[i];
            try {
                res[i] = this.objectMapper.readValue(str, SizeInventoryDTO.class);
            } catch (Exception e) {
                throw new InvalidFormat("Incorrect format " + e.getMessage());
            }
        }
        return res;
    }

}
