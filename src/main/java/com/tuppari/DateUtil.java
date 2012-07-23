package com.tuppari;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

abstract class DateUtil {

    private static Date dummyTime;

    /**
     * Format specified date as ISO 8601 style.
     *
     * TODO: fix after server module is updated to 0.2.0.
     *
     * @param date source date
     * @return ISO 8601 formatted string
     * @see http://www.w3.org/TR/NOTE-datetime
     */
    static String formatAsISO8601(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("date is null");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd\'T\'HHmmss\'Z\'", Locale.ENGLISH);
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return sdf.format(date);
    }

    /**
     * Format specified date as ISO 8601 style.
     *
     * TODO: fix after server module is updated to 0.2.0.
     *
     * @param date source date
     * @return RFC 3339 formatted string
     * @see http://www.ietf.org/rfc/rfc3339.txt
     */
    static String formatAsRFC3339(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("date is null");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return sdf.format(date);
    }

    static Date now() {
        if (dummyTime != null) {
            return dummyTime;
        }
        return new Date();
    }

    static void clearDummyTime() {
        dummyTime = null;
    }

    static void setDummyTime(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.ENGLISH);
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        try {
            dummyTime = sdf.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
