package com.tuppari;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DateUtilTest {

    @Test
    public void testFormatAsISO8601() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        Date date = sdf.parse("2012-01-02 12:23:34");

        String result = DateUtil.formatAsISO8601(date);
        assertThat(result, is("20120102T122334Z"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatAsISO8601_withNull() {
        DateUtil.formatAsISO8601(null);
    }

    @Test
    public void testFormatAsRFC3339() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        Date date = sdf.parse("2012-01-02 12:23:34");

        String result = DateUtil.formatAsRFC3339(date);
        assertThat(result, is("Mon, 02 Jan 2012 12:23:34 GMT"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatAsRFC3339_withNull() {
        DateUtil.formatAsRFC3339(null);
    }

}
