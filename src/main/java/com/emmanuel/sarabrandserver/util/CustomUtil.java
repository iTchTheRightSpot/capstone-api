package com.emmanuel.sarabrandserver.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

@Service @Setter @Getter
public class CustomUtil {

    private int maxSession = 3;

    /**
     * Converts date to UTC Date
     * @param date of type java.util.date
     * @return Date of type java.util.date
     * */
    public Optional<Date> toUTC(Date date) {
        if (date == null) {
            return Optional.empty();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        return Optional.of(calendar.getTime());
    }

}
